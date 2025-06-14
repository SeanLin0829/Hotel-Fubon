<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'edit' ? '編輯訂位' : '新增訂位'"
    width="420px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" ref="formRef" label-width="90px" :disabled="loading">
      <!-- 預約方式 -->
      <el-form-item label="預約方式">
        <el-radio-group v-model="form.type">
          <el-radio label="member">會員預約</el-radio>
          <el-radio label="guest">匿名預約</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 日期 -->
      <el-form-item label="預約日期" prop="dateOnly" required>
        <el-date-picker
          v-model="form.dateOnly"
          type="date"
          placeholder="選擇日期"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 時間 -->
      <el-form-item label="預約時間" prop="timeOnly" required>
        <el-time-select
          v-model="form.timeOnly"
          start="10:00"
          end="22:00"
          step="00:30"
          placeholder="選擇時間"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 人數 -->
      <el-form-item label="人數" prop="numberOfGuests" required>
        <el-input-number
          v-model="form.numberOfGuests"
          :min="1"
          :max="40"
          style="width: 120px"
        />
      </el-form-item>

      <!-- 桌號 -->
      <el-form-item label="桌號" prop="tableIds" required>
        <el-select
          v-model="form.tableIds"
          multiple
          placeholder="選擇桌號"
          style="width: 100%"
        >
          <el-option
            v-for="t in availableTables"
            :key="t.id"
            :label="`${t.name}（${t.capacity}人）`"
            :value="t.id"
          />
        </el-select>
      </el-form-item>

      <!-- 備註 -->
      <el-form-item label="備註">
        <el-input v-model="form.note" placeholder="備註" />
      </el-form-item>

      <!-- 顧客資訊 -->
      <template v-if="form.type === 'member'">
        <el-form-item label="顧客" prop="userId" required>
          <el-select
            v-model="form.userId"
            filterable
            allow-create
            default-first-option
            placeholder="請輸入或選擇顧客"
            style="width: 100%"
            :reserve-keyword="false"
            :value-key="'id'"
            :label-key="'name'"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="u.name"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="姓名" prop="guestName" required>
          <el-input v-model="form.guestName" />
        </el-form-item>
        <el-form-item label="電話" prop="guestPhone" required>
          <el-input v-model="form.guestPhone" />
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitForm"
        >確定</el-button
      >
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed, defineProps, defineEmits } from "vue";
import axios from "axios";
import { ElMessage } from "element-plus";

const props = defineProps({
  visible: Boolean,
  userOptions: Array,
  reservation: Object,
  mode: { type: String, default: "add" },
});
const emit = defineEmits(["update:visible", "success"]);

const visible = ref(props.visible);
watch(
  () => props.visible,
  (val) => (visible.value = val)
);
watch(visible, (val) => emit("update:visible", val));

// ===== 狀態 =====
const loading = ref(false);
const formRef = ref();
const form = reactive({
  type: "guest",
  userId: "",
  dateOnly: "",
  timeOnly: "",
  numberOfGuests: 1,
  tableIds: [],
  note: "",
  guestName: "",
  guestPhone: "",
});

const availableTables = ref([]);

// ===== 自動組合 datetime 格式 =====
const reservationTime = computed(() => {
  if (!form.dateOnly || !form.timeOnly) return "";

  const date = new Date(form.dateOnly);
  const pad = (n) => String(n).padStart(2, "0");
  const y = date.getFullYear();
  const m = pad(date.getMonth() + 1);
  const d = pad(date.getDate());

  return `${y}-${m}-${d}T${form.timeOnly}:00`;
});

function getEndTime(start) {
  if (!start) return "";
  const dt = new Date(start.replace(" ", "T"));
  dt.setHours(dt.getHours() + 2);
  const pad = (n) => String(n).padStart(2, "0");
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(
    dt.getDate()
  )}T${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`;
}

// ===== 查空桌 =====
watch([() => form.dateOnly, () => form.timeOnly], async () => {
  form.tableIds = [];
  availableTables.value = [];

  if (!form.dateOnly || !form.timeOnly) return; // ✅ 不完整就不送

  const start = reservationTime.value;
  const end = getEndTime(start);
  if (!start || !end || new Date(end) <= new Date(start)) return;

  try {
    const res = await axios.get(
      "http://localhost:8080/api/restaurant/available-tables",
      {
        params: { start, end },
      }
    );
    availableTables.value = res.data || [];
  } catch (e) {
    ElMessage.error("查詢空桌失敗");
  }
});

// ===== 編輯模式回填 =====
watch(
  () => props.reservation,
  async (val) => {
    if (val && props.mode === "edit") {
      const [datePart, timePart] = (val.reservationTime || "").split("T");
      form.dateOnly = datePart;
      form.timeOnly = timePart?.slice(0, 5) || "";
      form.numberOfGuests = val.peopleCount || val.numberOfGuests;
      form.tableIds = val.tables?.map((t) => t.id) || val.tableIds || [];
      form.note = val.note || "";
      form.guestName = val.guestName || "";
      form.guestPhone = val.guestPhone || "";
      form.type = val.userId ? "member" : "guest";
      form.userId = val.userId || "";

      // ✅ 強制查詢一次空桌（確保即使資料沒變也會查）
      if (form.dateOnly && form.timeOnly) {
        const start = reservationTime.value;
        const end = getEndTime(start);
        try {
          const res = await axios.get(
            "http://localhost:8080/api/restaurant/available-tables",
            {
              params: { start, end },
            }
          );
          availableTables.value = res.data || [];
        } catch (e) {
          ElMessage.error("查詢空桌失敗");
        }
      }
    } else if (props.mode === "add") {
      resetForm();
    }
  },
  { immediate: true }
);

// 預約方式切換
watch(
  () => form.type,
  (val) => {
    if (val === "member") {
      form.guestName = "";
      form.guestPhone = "";
    } else {
      form.userId = "";
    }
  }
);

function resetForm() {
  form.type = "guest";
  form.userId = "";
  form.dateOnly = "";
  form.timeOnly = "";
  form.numberOfGuests = 1;
  form.tableIds = [];
  form.note = "";
  form.guestName = "";
  form.guestPhone = "";
  availableTables.value = [];
  formRef.value?.resetFields();
}

function closeDialog() {
  visible.value = false;
  resetForm();
}

async function submitForm() {
  if (
    !reservationTime.value ||
    !form.numberOfGuests ||
    form.tableIds.length === 0 ||
    (form.type === "member" && !form.userId) ||
    (form.type === "guest" && (!form.guestName || !form.guestPhone))
  ) {
    ElMessage.warning("請填寫完整資料");
    return;
  }

  loading.value = true;
  const payload = {
    userId: form.type === "member" ? form.userId : null,
    reservationTime: reservationTime.value,
    numberOfGuests: form.numberOfGuests,
    tableIds: form.tableIds,
    note: form.note,
    guestName: form.type === "guest" ? form.guestName : null,
    guestPhone: form.type === "guest" ? form.guestPhone : null,
  };

  try {
    if (props.mode === "edit" && props.reservation?.id) {
      await axios.put(
        `http://localhost:8080/api/restaurant/${props.reservation.id}`,
        payload
      );
      ElMessage.success("修改成功！");
    } else {
      await axios.post("http://localhost:8080/api/restaurant", payload);
      ElMessage.success("新增成功！");
    }
    visible.value = false;
    emit("success");
    resetForm();
  } catch (e) {
    ElMessage.error("送出失敗：" + (e.response?.data?.message || e.message));
  } finally {
    loading.value = false;
  }
}
</script>
