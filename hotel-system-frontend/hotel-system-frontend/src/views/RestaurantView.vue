<template>
  <div class="reservation-list-wrapper">
    <!-- 頂部工具列 -->
    <div class="res-header">
      <div class="res-header-title">
        <div class="title">訂位列表</div>
        <div class="subtitle">查看過去 6 個月到未來 2 年內的訂位</div>
      </div>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="onAdd">
        <i class="el-icon-plus"></i> 新增訂位
      </el-button>
    </div>

    <div class="res-toolbar">
      <el-input
        v-model="searchText"
        placeholder="搜尋手機號碼或電子信箱"
        style="width: 220px"
        clearable
      >
        <template #prefix>
          <i class="el-icon-search"></i>
        </template>
      </el-input>
      <el-date-picker
        v-model="selectedDate"
        type="date"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        :clearable="false"
        style="width: 160px"
        @change="fetchReservations"
      />
      <el-select
        v-model="statusFilter"
        placeholder="訂位狀態"
        clearable
        style="width: 130px"
      >
        <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
      </el-select>
    </div>

    <!-- 總覽 -->
    <div class="res-overview">
      <div class="res-overview-item">
        <div class="res-overview-num">{{ stats.groupCount }}</div>
        <div class="res-overview-label">訂位組數</div>
      </div>
      <div class="res-overview-item">
        <div class="res-overview-num">{{ stats.peopleCount }}</div>
        <div class="res-overview-label">訂位人數</div>
      </div>
    </div>

    <!-- 訂位分組表 -->
    <div class="res-table">
      <div
        v-for="group in groupedReservations"
        :key="group.hour"
        class="res-group"
      >
        <div class="res-group-header">
          <div class="res-group-time">{{ group.hour }}:00</div>
          <div class="res-group-info">
            <span>
              <i class="el-icon-user"></i> {{ group.reservations.length }} 組
            </span>
            <span>
              <i class="el-icon-user-solid"></i> {{ group.totalPeople }} 位
            </span>
            <span>
              <i class="el-icon-user"></i> 人數 {{ group.adultCount }}
            </span>
          </div>
        </div>
        <div class="res-row" v-for="r in group.reservations" :key="r.id">
          <div class="res-col name">
            <span class="res-link">{{ r.name }}</span>
            <span class="phone">{{ r.phone }}</span>
          </div>
          <div class="res-col count">
            <span>{{ r.peopleCount }} 位</span>
            <span v-if="r.childrenCount > 0">、{{ r.childrenCount }} 小</span>
            <span v-if="r.note && r.note.includes('兒童椅')" class="child-seat">(兒童椅)</span>
          </div>
          <div class="res-col table">
            {{ r.tableNumbers?.join(", ") || "未安排" }}
          </div>
          <div class="res-col time">
            {{ formatTime(r.reservationTime) }}
          </div>
          <div class="res-col note">
            <span>{{ r.note || "--" }}</span>
          </div>
          <div class="res-col status">
            <span class="status-dot" :class="statusColorClass(r.status)"></span>
            <span class="status-label">{{ r.status }}</span>
          </div>
          <div class="res-col action">
            <!-- 狀態切換下拉在最左側 -->
            <el-select
              v-model="r._statusTemp"
              size="small"
              style="width: 110px; margin-right:8px;"
              @change="s => onChangeStatus(r, s)"
            >
              <el-option
                v-for="opt in statusChangeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-button type="success" size="small" @click="onEdit(r)">修改</el-button>
            <el-button type="danger" size="small" @click="onDelete(r)">刪除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增 Dialog -->
    <ReservationDialog
      v-model:visible="addDialogVisible"
      :user-options="userOptions"
      mode="add"
      @success="fetchReservations"
    />

    <!-- 編輯 Dialog -->
    <ReservationDialog
      v-model:visible="editDialogVisible"
      :user-options="userOptions"
      :reservation="editingReservation"
      mode="edit"
      @success="fetchReservations"
    />
  </div>
</template>


<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import ReservationDialog from "@/components/ReservationDialog.vue";

const statusOptions = [
  "全部",
  "已預訂",
  "已發送提醒",
  "已保留訂位",
  "已取消",
  "已帶位",
  "未出席",
];

const statusMap = {
  "BOOKED": "已預訂",
  "CANCELLED": "已取消",
  "COMPLETED": "用餐完畢",
  "CHECKED_IN": "已帶位"
};

const statusChangeOptions = [
  { value: "BOOKED", label: "已預訂" },
  { value: "CHECKED_IN", label: "已帶位" },
  { value: "COMPLETED", label: "用餐完畢" }
  
];

const selectedDate = ref(dayjs().format("YYYY-MM-DD"));
const statusFilter = ref("全部");
const searchText = ref("");
const reservationList = ref([]);

const userOptions = ref([]);

onMounted(async () => {
  await fetchReservations();       
  await fetchUserOptions();        
});

async function fetchUserOptions() {
  try {
    const res = await axios.get("/api/users/customer");
    userOptions.value = (res.data || []).map(user => ({
      id: user.id,
      name: user.fullName
    }));
  } catch (e) {
    ElMessage.error("載入會員清單失敗");
  }
}

const groupedReservations = computed(() => {
  let filtered = reservationList.value;
  if (statusFilter.value && statusFilter.value !== "全部") {
    filtered = filtered.filter((r) => r.status === statusFilter.value);
  }
  if (searchText.value) {
    const t = searchText.value.trim().toLowerCase();
    filtered = filtered.filter(
      (r) =>
        (r.name && r.name.toLowerCase().includes(t)) ||
        (r.phone && r.phone.toLowerCase().includes(t))
    );
  }
  const map = {};
  filtered.forEach((r) => {
    const hour = dayjs(r.reservationTime).format("HH");
    if (!map[hour]) map[hour] = [];
    map[hour].push(r);
  });
  return Object.entries(map)
    .sort(([a], [b]) => a - b)
    .map(([hour, reservations]) => ({
      hour,
      reservations,
      totalPeople: reservations.reduce(
        (sum, r) => sum + r.peopleCount + (r.childrenCount || 0),
        0
      ),
      adultCount: reservations.reduce((sum, r) => sum + r.peopleCount, 0),
      childCount: reservations.reduce(
        (sum, r) => sum + (r.childrenCount || 0),
        0
      ),
    }));
});

const stats = computed(() => {
  const list = groupedReservations.value.flatMap((g) => g.reservations);
  return {
    groupCount: list.length,
    peopleCount: list.reduce(
      (sum, r) => sum + r.peopleCount + (r.childrenCount || 0),
      0
    ),
  };
});

function statusColorClass(status) {
  switch (status) {
    case "已預訂":
      return "status-orange";
    case "用餐完畢":
      return "status-grey";
    case "已取消":
      return "status-red";
    case "已帶位":
      return "status-blue";
    case "未出席":
      return "status-dark";
    default:
      return "";
  }
}

async function onDelete(r) {
  if (!r.id) return;
  if (!confirm("確定要刪除此筆訂位嗎？")) return;
  try {
    await axios.delete(`/api/restaurant/${r.id}`);
    ElMessage.success("刪除成功！");
    await fetchReservations();
  } catch (e) {
    ElMessage.error("刪除失敗：" + (e.response?.data?.message || e.message));
  }
}

async function fetchReservations() {
  try {
    const res = await axios.get("/api/restaurant/by-date", {
      params: { date: selectedDate.value },
    });
    reservationList.value = (res.data || []).map((r) => ({
      id: r.id,
      name: r.user ? r.user.fullName : r.guestName,
      phone: r.user ? r.user.phone : r.guestPhone,
      peopleCount: r.numberOfGuests,
      childrenCount: r.childrenCount || 0,
      tableNumbers: (r.tables || []).map((t) => t.name),
      tables: r.tables || [],
      status: statusText(r.status),
      statusCode: r.status,
      _statusTemp: r.status,   // 狀態下拉選單綁定
      reservationTime: r.reservationTime,
      note: r.note || "",
      userId: r.user ? r.user.id : null,
      guestName: r.guestName || "",
      guestPhone: r.guestPhone || "",
    }));
  } catch (e) {
    ElMessage.error("載入訂位資料失敗");
    reservationList.value = [];
  }
}

function formatTime(dt) {
  return dayjs(dt).format('HH:mm');
}

function statusText(status) {
  return statusMap[status] || status;
}

async function onChangeStatus(r, newStatus) {
  if (r.statusCode === newStatus) return;
  try {
    await axios.put(`/api/restaurant/${r.id}/status`, null, {
      params: { status: newStatus }
    });
    ElMessage.success("狀態已更新");
    await fetchReservations();
  } catch (e) {
    ElMessage.error("狀態更新失敗：" + (e.response?.data?.message || e.message));
  }
}

// Dialog 狀態
const addDialogVisible = ref(false);
const editDialogVisible = ref(false);
const editingReservation = ref(null);

function onAdd() {
  addDialogVisible.value = true;
}
function onEdit(r) {
  editingReservation.value = r;
  editDialogVisible.value = true;
}

onMounted(fetchReservations);

</script>

<style scoped>
.reservation-list-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 0 36px 0;
}
.res-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}
.res-header-title .title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}
.res-header-title .subtitle {
  color: #888;
  font-size: 15px;
}
.res-toolbar {
  display: flex;
  gap: 16px;
  margin: 18px 0 22px 0;
}
.res-overview {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}
.res-overview-item {
  background: #fff;
  border-radius: 10px;
  padding: 16px 28px;
  min-width: 130px;
  text-align: center;
  box-shadow: 0 2px 10px #0001;
}
.res-overview-num {
  font-size: 32px;
  font-weight: 700;
  color: #3578d1;
}
.res-overview-label {
  color: #666;
  margin-top: 4px;
}
.res-table {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 0 0 0;
}
.res-group {
  margin-bottom: 12px;
}
.res-group-header {
  display: flex;
  align-items: center;
  background: #f1f6fb;
  padding: 8px 20px;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #e2e6ea;
  font-weight: 600;
  font-size: 16px;
  gap: 22px;
}
.res-group-time {
  font-weight: bold;
  color: #316cba;
  font-size: 17px;
  min-width: 58px;
}
.res-group-info span {
  margin-right: 16px;
  color: #444;
  font-size: 15px;
}
.res-row {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f3f3f3;
  min-height: 46px;
  transition: background 0.2s;
}
.res-row:last-child {
  border-radius: 0 0 10px 10px;
  border-bottom: none;
}
.res-col {
  padding: 6px 8px;
  font-size: 15px;
  flex: 1;
  display: flex;
  align-items: center;
}
.res-col.name {
  min-width: 170px;
  flex: 1.3;
  flex-direction: column;
  align-items: flex-start;
}
.res-link {
  color: #3578d1;
  font-weight: 500;
  cursor: pointer;
}
.res-col .phone {
  color: #888;
  font-size: 13px;
  margin-left: 0;
}
.res-col.count {
  min-width: 95px;
}
.child-seat {
  color: #4caf50;
  margin-left: 2px;
  font-size: 12px;
}
.res-col.table {
  min-width: 88px;
}
.res-col.note {
  flex: 1.4;
  min-width: 130px;
  color: #888;
  font-size: 14px;
}
.res-col.status {
  min-width: 90px;
}

.res-col.action .el-button {
  font-size: 15px;
  padding: 2px 8px;
  font-weight: 500;
}
.res-col.action .el-button--text {
  color: #3578d1;
}
.res-col.action .el-button--text:hover {
  color: #ff5c5c;
  text-decoration: underline;
}
.status-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 7px;
  border-radius: 50%;
  vertical-align: middle;
}
.status-red {
  background: #ff5c5c;
}
.status-orange {
  background: #ffb43a;
}
.status-green {
  background: #84cf8a;
}
.status-blue {
  background: #359aff;
}
.status-grey {
  background: #c1c9d3;
}
.status-dark {
  background: #7d889c;
}
.status-label {
  font-size: 15px;
  font-weight: 500;
  color: #444;
  vertical-align: middle;
}
.res-col.action {
  min-width: 75px;
  gap: 8px;
  justify-content: flex-end;
}
@media (max-width: 720px) {
  .reservation-list-wrapper {
    padding: 6px 2px 28px 2px;
    max-width: 100vw;
  }
  .res-header-title .title {
    font-size: 21px;
  }
  .res-group-header,
  .res-row {
    font-size: 14px;
    padding: 4px 6px;
  }
  .res-table {
    padding: 0;
  }
}

.res-col.time {
  min-width: 125px;
  color: #316cba;
  font-weight: 500;
  font-size: 15px;
}

.res-row {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f3f3f3;
  min-height: 46px;
  transition: background 0.2s;
}

.res-row:last-child {
  border-radius: 0 0 10px 10px;
  border-bottom: none;
}

/* 這一段是新增的關鍵！ */
.res-col {
  padding: 6px 8px;
  font-size: 15px;
  flex: 1;
  display: flex;
  align-items: center;
  border-right: 1px solid #e3e7ef;
}

/* 最後一格不要 border */
.res-row .res-col:last-child {
  border-right: none;
}
</style>
