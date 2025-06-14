<template>
  <div
  class="schedule-table-container"
  v-loading="loading"
  element-loading-text="資料載入中..."
  element-loading-background="rgba(255, 255, 255, 0.6)"
  :element-loading-spinner="null"
  :fullscreen="true">
    <div class="table-header">
      <el-date-picker
        v-model="monthValue"
        type="month"
        placeholder="選擇月份"
        format="YYYY-MM"
        value-format="YYYY-MM"
        @change="onMonthChange"
        :clearable="false"
        style="width: 140px"
      />
      <span class="header-title"> 員工排班表 </span>
      <div class="shift-info">
        <div
          v-for="shift in shiftTypes"
          :key="shift.code"
          class="shift-info-item"
        >
          <span
            :style="{
              background: shift.color,
              color: shift.code === 'X' ? '#333' : '#fff',
            }"
            class="badge"
          >
            {{ shift.name }}
          </span>
          <span class="time">{{ shiftTimeLabel(shift.code) }}</span>
        </div>
      </div>
    </div>
    <el-table
      v-loading="loading"
      :data="tableData"
      class="schedule-table"
      border
      v-if="!loading && tableData.length"
      :summary-method="tableSummary"
      show-summary
    >
      <el-table-column prop="name" label="員工" fixed width="120" />
      <el-table-column
        v-for="day in daysInMonth"
        :key="day"
        align="center"
        width="55"
      >
        <template #header>
          <div>
            <div
              :style="{
                background: isWeekend(day) ? '#ffd700' : '#e6e6e6',
                fontWeight: 'bold',
              }"
            >
              {{ day }}
            </div>
            <div
              :style="{
                color: isWeekend(day) ? '#b46b01' : '#212121',
                fontWeight: isWeekend(day) ? 'bold' : 'normal',
              }"
            >
              {{ weekLabel(day) }}
            </div>
          </div>
        </template>
        <template #default="scope">
          <template v-if="isAdmin">
            <el-popover placement="bottom" trigger="click" width="120">
              <div style="display: flex; flex-direction: column">
                <el-button
                  v-for="shift in shiftTypes"
                  :key="shift.code"
                  size="small"
                  @click="selectShift(scope.row, day, shift.code)"
                  :style="{
                    margin: '3px 0',
                    background: shift.color,
                    color: shift.code === 'X' ? '#333' : '#fff',
                  }"
                >
                  {{ shift.name }}
                </el-button>
                <el-button
                  size="small"
                  plain
                  @click="selectShift(scope.row, day, '')"
                  >清除</el-button
                >
              </div>
              <template #reference>
                <span
                  class="shift-cell"
                  :class="shiftClass(scope.row['d' + day])"
                  :style="{ cursor: 'pointer', border: '1px solid #dcdfe6' }"
                >
                  {{ displayShift(scope.row["d" + day]) }}
                </span>
              </template>
            </el-popover>
          </template>
          <template v-else>
            <span
              class="shift-cell"
              :class="shiftClass(scope.row['d' + day])"
              style="
                border: 1px solid #dcdfe6;
                cursor: not-allowed;
                opacity: 0.7;
              "
              title="僅管理員可編輯"
            >
              {{ displayShift(scope.row["d" + day]) }}
            </span>
          </template>
        </template>
      </el-table-column>
      <el-table-column
        prop="total"
        label="出勤天數"
        width="70"
        align="center"
      />
      <el-table-column
        v-for="shift in shiftTypes"
        :key="shift.code"
        :prop="shift.code + '_count'"
        :label="shift.name + '數'"
        width="58"
        align="center"
      />
    </el-table>
    <el-empty
      v-else-if="!loading && !tableData.length"
      description="暫無資料"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import axios from "axios";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import { nextTick } from 'vue';
const userStore = useUserStore();
const isAdmin = computed(() => userStore.isAdmin);

// 1. 班別定義
const shiftTypes = [
  { code: "E", name: "早班", color: "#67c23a" },
  { code: "D", name: "中班", color: "#409eff" },
  { code: "N", name: "晚班", color: "#e6a23c" },
  { code: "X", name: "休假", color: "#ffd700" },
];
const code2api = { E: "morning", D: "evening", N: "night", X: "off" };
const api2code = { morning: "E", evening: "D", night: "N", off: "X" };

const loading = ref(true);
const monthValue = ref(dayjs().format("YYYY-MM"));
const daysInMonth = computed(() => {
  const days = dayjs(`${monthValue.value}-01`).daysInMonth();
  return Array.from({ length: days }, (_, i) => i + 1);
});

const staffList = ref([]); // 所有員工
const scheduleMap = ref({}); // 員工 -> { day: {code, scheduleId} }
const tableData = ref([]);

// 1. 抓員工
async function fetchStaff() {
  try {
    const res = await axios.get("/api/users/staff");
    staffList.value = res.data;
  } catch (e) {
    ElMessage.error("員工資料載入失敗");
    staffList.value = [];
  }
}

// 2. 抓班表
async function fetchSchedules() {
  loading.value = true;
  try {
    const [year, month] = monthValue.value.split("-");
    const res = await axios.get(`/api/schedules/monthly`, {
      params: { year, month },
    });
    // { employeeId: { day: {code, scheduleId} } }
    const map = {};
    for (const sch of res.data) {
      const code = api2code[sch.shiftType] || "";
      const day = dayjs(sch.shiftDate).date();
      if (!map[sch.employeeId]) map[sch.employeeId] = {};
      map[sch.employeeId][day] = {
        code,
        scheduleId: sch.scheduleId, // 假設後端傳 scheduleId
      };
    }
    scheduleMap.value = map;
    buildTable();
  } catch (e) {
    ElMessage.error("班表資料載入失敗");
    scheduleMap.value = {};
    tableData.value = [];
  } finally {
    loading.value = false;
  }
}



// 3. 整合名單/班表
function buildTable() {
  const days = daysInMonth.value;
  tableData.value = staffList.value.map((staff) => {
    const row = { id: staff.id, name: staff.fullName };
    const empSch = scheduleMap.value[staff.id] || {};
    days.forEach((day) => {
      row["d" + day] = empSch[day]?.code || "";
      row["d" + day + "_id"] = empSch[day]?.scheduleId || null;
    });
    shiftTypes.forEach((shift) => {
      row[shift.code + "_count"] = 0;
    });
    row.total = 0;
    return row;
  });
  calcStats();
}

// 4. 點班別時自動決定 POST/PUT
async function selectShift(row, day, code) {
  const employeeId = row.id;
  const date = `${monthValue.value}-${String(day).padStart(2, "0")}`;
  const shiftType = code2api[code];
  const note = "";
  const scheduleId = row["d" + day + "_id"];

  // 清除班表
  if (!code) {
    try {
      if (scheduleId) {
        await axios.delete(`/api/schedules/${scheduleId}`);
      }
      row["d" + day] = "";
      row["d" + day + "_id"] = null;
      calcStats();
      ElMessage.success("已清除");
    } catch (e) {
      const msg = e.response?.data?.message || "清除失敗";
      ElMessage.error(msg);
    }
    return;
  }

  // 新增或更新班表
  try {
    if (scheduleId) {
      // PUT 修改
      await axios.put(`/api/schedules/${scheduleId}`, {
        employeeId,
        shiftDate: date,
        shiftType,
        note,
        status: "scheduled",
      });
      row["d" + day] = code;
      ElMessage.success("已更新");
    } else {
      // POST 新增
      const res = await axios.post("/api/schedules", {
        employeeId,
        shiftDate: date,
        shiftType,
        note,
      });
      row["d" + day] = code;
      row["d" + day + "_id"] = res.data.id;
      ElMessage.success("已儲存");
    }
    calcStats();
  } catch (e) {
    const msg = e.response?.data?.message || "儲存失敗";
    ElMessage.error(msg);
  }
}


function shiftTimeLabel(code) {
  switch (code) {
    case "E":
      return "06:00~14:00";
    case "D":
      return "14:00~22:00";
    case "N":
      return "22:00~06:00";
    case "X":
      return "休假";
    default:
      return "";
  }
}

// 統計
function calcStats() {
  tableData.value.forEach((row) => {
    let total = 0;
    shiftTypes.forEach((shift) => {
      row[shift.code + "_count"] = 0;
    });
    for (let d = 1; d <= daysInMonth.value.length; d++) {
      const v = row["d" + d];
      if (v) {
        row[v + "_count"]++;
        // 只要不是休假(X)才算出勤
        if (v !== "X") total++;
      }
    }
    row.total = total;
  });
}
function isWeekend(day) {
  const date = dayjs(`${monthValue.value}-${String(day).padStart(2, "0")}`);
  return date.day() === 0 || date.day() === 6;
}
function weekLabel(day) {
  const weekArr = ["日", "一", "二", "三", "四", "五", "六"];
  const date = dayjs(`${monthValue.value}-${String(day).padStart(2, "0")}`);
  return weekArr[date.day()];
}
async function onMonthChange() {
  loading.value = true;
  await nextTick(); // 讓 DOM 有機會先畫出 loading 畫面
  try {
    await fetchSchedules();
  } catch (e) {
    ElMessage.error('切換月份時發生錯誤');
  } finally {
    loading.value = false;
  }
}

// watch

onMounted(() => {
  safeLoad(async () => {
    await fetchStaff();
    await fetchSchedules();
  });
});

// cell顯示
function displayShift(code) {
  if (!code) return "";
  const shift = shiftTypes.find((s) => s.code === code);
  return shift ? shift.name[0] : code;
}
function shiftClass(code) {
  if (!code) return "";
  const shift = shiftTypes.find((s) => s.code === code);
  return shift ? "shift-" + shift.code : "";
}
async function safeLoad(taskFn) {
  loading.value = true;
  const start = Date.now();
  await taskFn();
  const duration = Date.now() - start;
  const wait = Math.max(300 - duration, 0);
  setTimeout(() => {
    loading.value = false;
  }, wait);
}

function tableSummary({ columns, data }) {
  const summary = ["當日班別統計"];
  const colCount = columns.length;
  const dayCounts = daysInMonth.value.map((day) => ({
    E: 0,
    D: 0,
    N: 0,
    X: 0,
  }));
  data.forEach((row) => {
    daysInMonth.value.forEach((day, idx) => {
      const code = row["d" + day];
      if (["E", "D", "N", "X"].includes(code)) {
        dayCounts[idx][code]++;
      }
    });
  });
  daysInMonth.value.forEach((day, idx) => {
    let show = "";
    if (dayCounts[idx].E) show += `早${dayCounts[idx].E} `;
    if (dayCounts[idx].D) show += `中${dayCounts[idx].D} `;
    if (dayCounts[idx].N) show += `晚${dayCounts[idx].N} `;
    summary.push(show.trim());
  });
  while (summary.length < colCount) summary.push("");
  return summary;
}
</script>

<style scoped>
.schedule-table-container {
  padding: 24px 12px;
  background: #f9f9fd;
}
.table-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 16px;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
  margin-left: 18px;
  letter-spacing: 2px;
  color: #333;
}
.schedule-table {
  border-radius: 8px;
  box-shadow: 0 2px 12px #d6e2f17a;
  background: #fff;
  font-size: 15px;
}
.el-table .el-table__cell {
  padding: 4px 2px;
  font-size: 15px;
}
.shift-cell {
  display: inline-block;
  min-width: 36px;
  min-height: 22px;
  text-align: center;
  border-radius: 5px;
  transition: box-shadow 0.2s;
}
.shift-E {
  background: #67c23a;
  color: #fff;
}
.shift-D {
  background: #409eff;
  color: #fff;
}
.shift-N {
  background: #e6a23c;
  color: #fff;
}
.shift-X {
  background: #ffd700;
  color: #333;
}
.shift-cell:hover {
  box-shadow: 0 0 0 2px #409eff44;
  filter: brightness(1.05);
}
.weekend-header {
  background: #f9ede3 !important;
}

.shift-info {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.shift-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}

.time {
  color: #666;
  font-size: 13px;
}

.schedule-table-container {
  min-height: 100vh; /* 根據畫面大小調整 */
}
</style>
