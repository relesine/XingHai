<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">上架新商品</el-button>
      <span class="toolbar-end muted">共 {{ products.length }} 件商品</span>
    </div>
    <el-table :data="products" v-loading="loading">
      <el-table-column label="主图" width="70">
        <template #default="{ row }"><img :src="row.coverUrl" class="p-cover" /></template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="160" />
      <el-table-column label="价格" width="100">
        <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在售' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '上架新商品'" width="520">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0.01" :precision="2"
            :step="1" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="主图">
          <el-upload :auto-upload="false" :limit="1" accept="image/*" :on-change="onCover">
            <el-button>选择图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { apiMyShop, apiShopProducts, apiAddProduct, apiUpdateProduct, apiProductStatus, apiDeleteProduct } from '../api'

const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ id: null, productName: '', price: 9.9, stock: 100, coverUrl: '', description: '' })

async function load() {
  loading.value = true
  try {
    const shop = await apiMyShop()
    products.value = shop ? (await apiShopProducts(shop.id, 1)).list : []
  } finally { loading.value = false }
}

function openAdd() {
  editing.value = false
  form.value = { id: null, productName: '', price: 9.9, stock: 100, coverUrl: '', description: '' }
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}
async function onCover(file) {
  if (!file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  fd.append('dir', 'cover')
  form.value.coverUrl = await request.post('/upload/image', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  ElMessage.success('图片已上传')
}
async function save() {
  if (editing.value) {
    await apiUpdateProduct(form.value.id, form.value)
  } else {
    await apiAddProduct(form.value)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  load()
}
async function toggle(row) {
  await apiProductStatus(row.id, row.status === 1 ? 0 : 1)
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  await apiDeleteProduct(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.p-cover {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 6px;
  display: block;
}

.price {
  color: var(--danger);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
</style>
