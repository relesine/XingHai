<template>
  <div class="page-card">
    <div class="toolbar">
      <span class="muted">角色决定管理员可访问的功能范围，权限调整后立即生效</span>
    </div>
    <el-table :data="roles" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="code" label="角色编码" width="120">
        <template #default="{ row }"><el-tag>{{ row.code }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="name" label="角色名称" width="140" />
      <el-table-column prop="remark" label="说明" min-width="180" />
      <el-table-column label="已授权限" min-width="240">
        <template #default="{ row }">
          <el-tag v-for="p in row.permissions" :key="p" size="small" type="info" style="margin:2px 4px 2px 0">{{ p }}</el-tag>
          <span v-if="!row.permissions.length" class="none">未授权</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openAuth(row)">授权</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="`授权：${current?.name} (${current?.code})`" width="480">
      <el-checkbox-group v-model="checked">
        <el-checkbox v-for="p in permissions" :key="p.id" :value="p.id" style="display:block;margin-bottom:8px">
          {{ p.name }}（{{ p.code }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRoles, apiPermissions, apiAssignPermissions } from '../api'

const roles = ref([])
const permissions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const current = ref(null)
const checked = ref([])

async function load() {
  loading.value = true
  try {
    roles.value = await apiRoles()
    permissions.value = await apiPermissions()
  } finally { loading.value = false }
}
async function openAuth(row) {
  current.value = row
  // 用权限编码匹配当前角色已有的权限点
  checked.value = permissions.value.filter(p => row.permissions.includes(p.code)).map(p => p.id)
  dialogVisible.value = true
}
async function save() {
  await apiAssignPermissions(current.value.id, checked.value)
  ElMessage.success('授权成功')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.none { color: var(--text-4); font-size: 12px; }
</style>
