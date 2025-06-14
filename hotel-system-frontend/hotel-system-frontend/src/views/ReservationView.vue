<template>
  <div class="reservation-container">
    <!-- 搜尋欄 -->
    <div class="search-bar-wrapper">
      <div
        class="search-bar"
        style="display: flex; gap: 8px; margin-bottom: 24px"
      >
        <el-input
          v-model="search.name"
          placeholder="訂房人"
          clearable
          style="width: 200px"
        />
        <el-input
          v-model="search.roomNumber"
          placeholder="房號/房型"
          clearable
          style="width: 120px"
        />
        <el-input-number
          v-model="search.guestCount"
          :min="1"
          placeholder="人數"
          style="width: 120px"
        />
        <el-date-picker
          v-model="search.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住"
          end-placeholder="退房"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
        <el-select
          v-model="search.status"
          placeholder="狀態"
          clearable
          style="width: 100px"
        >
          <el-option label="全部" value="" />
          <el-option label="預約中" value="預約中" />
          <el-option label="已取消" value="已取消" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜尋</el-button>
        <el-button @click="handleReset" type="default">清除</el-button>
        <el-button type="success" @click="openCreateDialog">新增訂房</el-button>
      </div>
    </div>

    <!-- 訂房列表 -->
    <el-table
      :data="filteredReservations"
      border
      stripe
      style="width: 100%"
      :row-class-name="tableRowClass"
    >
      <el-table-column prop="id" label="訂單編號" width="100" />

      <el-table-column prop="userName" label="訂房人" width="200" />
      <el-table-column prop="checkinDate" label="入住日期" width="200" />
      <el-table-column prop="checkoutDate" label="退房日期" width="200" />
      <el-table-column prop="guestCount" label="人數" width="80" />
      <el-table-column prop="totalPrice" label="總價" width="100" />
      <el-table-column label="房型">
        <template #default="scope">
          <el-tag
            v-for="(room, idx) in scope.row.roomNames"
            :key="idx"
            type="info"
            style="margin-right: 4px"
          >
            {{ room }} - {{ scope.row.roomNumbers?.[idx] || "房號不詳" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="note" label="備註" width="300" />
      <el-table-column label="狀態" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" effect="light">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <div style="display: flex; gap: 8px; flex-wrap: wrap">
             <el-dropdown @command="(val) => updateStatus(scope.row.id, val)">
              <el-button size="small" type="warning">
                更改狀態<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="預約中">預約中</el-dropdown-item>
                  <el-dropdown-item command="已入住">已入住</el-dropdown-item>
                  <el-dropdown-item command="已退房">已退房</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              size="small"
              type="primary"
              @click="editReservation(scope.row)"
              >編輯</el-button
            >

            <el-button
              size="small"
              type="danger"
              @click="cancelReservation(scope.row.id)"
              >取消</el-button
            >
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 / 修改共用 Dialog -->
    <el-dialog
      :title="isEditing ? '修改訂房' : '新增訂房'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <!-- 訂房人（會員搜尋＋手動輸入） -->
        <el-form-item label="訂房人">
          <el-select
            v-model="form.userId"
            filterable
            allow-create
            default-first-option
            placeholder="請輸入或選擇會員姓名"
            style="width: 100%"
          >
            <el-option
              v-for="c in customerOptions"
              :key="c.id"
              :label="`${c.name}（${c.phone}）`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <!-- 非會員欄位（僅當未選擇會員時顯示） -->
        <template v-if="!form.userId">
          <el-form-item label="姓名" prop="guestName">
            <el-input v-model="form.guestName" placeholder="非會員必填" />
          </el-form-item>
          <el-form-item label="電話" prop="guestPhone">
            <el-input v-model="form.guestPhone" placeholder="非會員必填" />
          </el-form-item>
        </template>

        <!-- 其他欄位 -->
        <el-form-item label="入住日期" prop="checkinDate">
          <el-date-picker
            v-model="form.checkinDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="退房日期" prop="checkoutDate">
          <el-date-picker
            v-model="form.checkoutDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="人數" prop="guestCount">
          <el-input-number v-model="form.guestCount" :min="1" />
        </el-form-item>

        <el-form-item label="備註">
          <el-input type="textarea" v-model="form.note" />
        </el-form-item>

        <el-form-item label="房間" prop="roomIds">
          <el-select v-model="form.roomIds" multiple placeholder="選擇房間">
            <el-option
              v-for="room in roomOptions"
              :key="room.value"
              :label="room.label"
              :value="room.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- footer -->
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEditing ? "確認修改" : "送出" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import api from "@/api/axios";
import { ElMessage, ElMessageBox } from "element-plus";

// 搜尋狀態
const search = ref({
  name: "",
  roomNumber: "",
  guestCount: null,
  dateRange: [],
  status: "",
});

const reservationList = ref([]);
const roomTypeData = ref([]);
const roomOptions = ref([]);

const dialogVisible = ref(false);
const isEditing = ref(false);
const editingReservationId = ref(null);
const formRef = ref();

const form = ref({
  userId: null,
  userName: "", // ✅ 補上會員名稱
  guestName: "",
  guestPhone: "",
  checkinDate: "",
  checkoutDate: "",
  guestCount: 1,
  note: "",
  roomIds: [],
});

const customerOptions = ref([]);

async function fetchCustomers() {
  try {
    const res = await api.get("/users/customer");
    customerOptions.value = res.data.map((u) => ({
      id: u.id,
      name: u.fullName,
      phone: u.phone,
    }));
  } catch (e) {
    ElMessage.error("載入會員清單失敗");
  }
}

watch(
  () => [form.value.checkinDate, form.value.checkoutDate],
  async ([checkin, checkout]) => {
    if (!checkin || !checkout) return;
    try {
      const res = await api.get("/rooms/available", {
        params: {
          checkin,
          checkout,
        },
      });

      const availableRooms = res.data;

      roomOptions.value = availableRooms.map((room) => ({
        value: room.id,
        label: `${room.roomNumber} - ${room.type.name}`,
      }));
    } catch (err) {
      ElMessage.error("查詢可用房間失敗");
    }
  }
);

onMounted(() => {
  fetchReservations();
  fetchRoomOptions();
  fetchCustomers(); // 新增
});

const rules = {
  userId: [{ required: true, message: "請輸入使用者 ID", trigger: "blur" }],
  checkinDate: [
    { required: true, message: "請選擇入住日期", trigger: "change" },
  ],
  checkoutDate: [
    { required: true, message: "請選擇退房日期", trigger: "change" },
  ],
  guestCount: [
    { required: true, type: "number", message: "請填寫人數", trigger: "blur" },
  ],
  roomIds: [
    {
      required: true,
      type: "array",
      message: "請選擇至少一間房",
      trigger: "change",
    },
  ],
};

// 取得資料
onMounted(() => {
  fetchReservations();
  fetchRoomOptions();
});

async function fetchReservations() {
  try {
    const res = await api.get("/reservations");
    reservationList.value = res.data;
  } catch (err) {
    ElMessage.error("載入訂房資料失敗");
  }
}

async function fetchRoomOptions() {
  try {
    const res = await api.get("/reservations/with-rooms");
    roomTypeData.value = res.data;
    roomOptions.value = res.data.flatMap((type) =>
      type.rooms.map((room) => ({
        label: `${room.roomNumber} - ${type.typeName}`,
        value: room.roomId,
      }))
    );
  } catch (err) {
    ElMessage.error("取得房型失敗");
  }
}

// 進階多欄位搜尋
const filteredReservations = computed(() => {
  return reservationList.value
    .filter((r) => {
      // 訂房人 無視大小寫 模糊搜尋
      if (
        search.value.name &&
        !r.userName?.toLowerCase().includes(search.value.name.toLowerCase())
      )
        return false;
      // 房號/房型
      if (search.value.roomNumber) {
        const hasRoom =
          r.roomNumbers?.some((num) =>
            num?.includes(search.value.roomNumber)
          ) ||
          r.roomNames?.some((name) => name?.includes(search.value.roomNumber));
        if (!hasRoom) return false;
      }
      // 人數
      if (search.value.guestCount && r.guestCount !== search.value.guestCount)
        return false;
      // 狀態
      if (search.value.status && r.status !== search.value.status) return false;
      // 入住日期區間
      if (search.value.dateRange?.length === 2) {
        const [start, end] = search.value.dateRange;
        if (r.checkinDate < start || r.checkinDate > end) return false;
      }
      return true;
    })
    .sort((a, b) => new Date(a.checkinDate) - new Date(b.checkinDate));
});

function handleSearch() {
  // computed 自動 reactive，不需特別處理
}
function handleReset() {
  search.value = {
    name: "",
    roomNumber: "",
    guestCount: null,
    dateRange: [],
    status: "",
  };
}

// Dialog 開啟
function openCreateDialog() {
  isEditing.value = false;
  dialogVisible.value = true;
  form.value = {
    userId: null,
    checkinDate: "",
    checkoutDate: "",
    guestCount: 1,
    note: "",
    roomIds: [],
  };
}

function getStatusTagType(status) {
  switch (status) {
    case "預約中":
      return "warning";
    case "已入住":
      return "success";
    case "已退房":
      return "info";
    case "已取消":
      return "danger";
    default:
      return "default";
  }
}

async function updateStatus(reservationId, newStatus) {
  try {
    await api.patch(`/reservations/${reservationId}/status`, null, {
      params: { status: newStatus },
    });
    ElMessage.success(`狀態已更新為 ${newStatus}`);
    fetchReservations();
  } catch (err) {
    ElMessage.error("狀態更新失敗");
  }
}
// 編輯
async function editReservation(row) {
  if (row.status === "已取消") {
    try {
      await ElMessageBox.confirm("該訂單已取消，確定要編輯？", "提醒", {
        confirmButtonText: "確定",
        cancelButtonText: "放棄",
        type: "warning",
      });
      // 如果用戶點「確定」就繼續往下執行
    } catch {
      // 點「放棄」就直接 return，不做任何事
      return;
    }
  }
  isEditing.value = true;
  dialogVisible.value = true;
  editingReservationId.value = row.id;
  try {
    const res = await api.get(`/reservations/${row.id}`);
    const data = res.data;
    form.value = {
      userId: data.userId ?? null,
      userName: data.userName ?? "", // ✅ 從後端塞入
      guestName: data.guestName ?? "",
      guestPhone: data.guestPhone ?? "",
      checkinDate: data.checkinDate,
      checkoutDate: data.checkoutDate,
      guestCount: data.guestCount,
      note: data.note,
      roomIds: data.roomIds ?? [],
    };
  } catch (err) {
    ElMessage.error("載入訂單資料失敗");
    dialogVisible.value = false;
  }
}

// 查看
function viewReservation(row) {
  ElMessage.info(`查看訂單 ${row.id}（尚未實作）`);
}

// 取消
async function cancelReservation(id) {
  try {
    await ElMessageBox.confirm(
      `確定要取消訂單 #${id} 嗎？此操作將無法還原。`,
      "警告",
      {
        confirmButtonText: "確定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    await api.delete(`/reservations/${id}`);
    ElMessage.success(`已成功取消訂單 #${id}`);
    fetchReservations();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error("取消訂單失敗");
    }
  }
}
// 新增／修改 submit
function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    try {
      if (isEditing.value) {
        await api.patch(
          `/reservations/${editingReservationId.value}`,
          form.value
        );
        ElMessage.success("修改成功");
      } else {
        await api.post("/reservations", form.value);
        ElMessage.success("新增成功");
      }
      dialogVisible.value = false;
      fetchReservations();
    } catch (err) {
      const msg = err?.response?.data?.message || "操作失敗";
      ElMessage.error(msg);
    }
  });
}
</script>

<style scoped>
.reservation-container {
  padding: 24px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
}

.search-bar {
  width: 1100px;
}

.search-bar-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
  scale: 1.1;
}
</style>
