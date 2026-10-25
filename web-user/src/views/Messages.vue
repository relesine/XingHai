<template>
  <div class="page-container">
    <div class="page-heading">
      <h1>消息中心</h1>
      <p class="muted">与创作者和其他用户的私信沟通</p>
    </div>

    <div class="msg-body">
      <aside class="conv-pane">
        <div class="conv-title">全部会话</div>
        <div class="conv-list">
          <button
            v-for="c in conversations"
            :key="c.userId"
            class="conv"
            :class="{ active: current === c.userId }"
            @click="open(c.userId)"
          >
            <img :src="c.avatar || '/upload/avatar/u1.svg'" class="c-avatar" alt="" />
            <div class="c-info">
              <div class="c-top">
                <span class="c-name">{{ c.nickname }}</span>
                <span v-if="c.unread" class="c-unread">{{ c.unread > 99 ? '99+' : c.unread }}</span>
              </div>
              <div class="c-last">{{ c.lastContent }}</div>
            </div>
          </button>
          <div v-if="!conversations.length" class="conv-empty">暂无会话</div>
        </div>
      </aside>

      <section class="chat-pane">
        <header v-if="peer" class="chat-head">
          <img :src="peer.avatar || '/upload/avatar/u1.svg'" class="h-avatar" alt="" />
          <div class="h-meta">
            <div class="h-name">{{ peer.nickname }}</div>
            <div class="h-sub">私信</div>
          </div>
          <button class="h-link" @click="$router.push(`/user/${peer.id || peer.userId}`)">查看主页</button>
        </header>

        <div ref="chatList" class="chat-list">
          <div v-for="m in messages" :key="m.id" class="msg" :class="{ mine: m.fromUserId === myId }">
            <div class="bubble">{{ m.content }}</div>
            <div class="m-time">{{ fmt(m.createTime) }}</div>
          </div>
          <div v-if="!messages.length" class="chat-empty">
            <AppIcon name="message" :size="36" />
            <p>{{ peer ? '还没有消息，发条消息打个招呼吧' : '从左侧选择一个会话开始聊天' }}</p>
          </div>
        </div>

        <footer v-if="peer" class="chat-input">
          <el-input
            v-model="text"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="500"
            placeholder="输入消息，按 Enter 发送"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" :disabled="!text.trim()" @click="send">发送</el-button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { apiConversations, apiMessages, apiSendMessage, apiUserInfo } from '../api'
import { useUserStore } from '../stores/user'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const userStore = useUserStore()
const conversations = ref([])
const messages = ref([])
const current = ref(null)
const peer = ref(null)
const text = ref('')
const myId = userStore.user?.userId
const chatList = ref(null)
let timer = null

const fmt = t => (t || '').replace('T', ' ').slice(11, 16)

async function refreshConvs() {
  conversations.value = await apiConversations()
}

async function open(userId) {
  current.value = userId
  peer.value = await apiUserInfo(userId)
  messages.value = await apiMessages(userId)
  refreshConvs()
  scrollBottom()
}

async function send() {
  if (!text.value.trim()) return
  await apiSendMessage({ toUserId: current.value, content: text.value.trim() })
  text.value = ''
  messages.value = await apiMessages(current.value)
  scrollBottom()
}

function scrollBottom() {
  nextTick(() => {
    if (chatList.value) chatList.value.scrollTop = chatList.value.scrollHeight
  })
}

onMounted(async () => {
  await refreshConvs()
  // 支持从他人主页带参跳转：?to=userId 直接打开会话
  const to = route.query.to
  if (to) {
    await open(Number(to))
  } else if (conversations.value.length) {
    await open(conversations.value[0].userId)
  }
  // 每 5 秒轮询消息与会话列表（生产环境应改用 WebSocket）
  timer = setInterval(async () => {
    if (current.value) {
      messages.value = await apiMessages(current.value)
      scrollBottom()
    }
    refreshConvs()
  }, 5000)
})

onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.msg-body {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  height: calc(100vh - 220px);
  min-height: 480px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.conv-pane {
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.conv-title {
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  border-bottom: 1px solid var(--border-light);
}
.conv-list { flex: 1; overflow-y: auto; min-height: 0; }
.conv {
  width: 100%;
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border: 0;
  border-bottom: 1px solid var(--border-light);
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: background .15s;
}
.conv:hover { background: #fafbfc; }
.conv.active { background: var(--brand-light); }
.c-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.c-info { flex: 1; min-width: 0; }
.c-top { display: flex; align-items: center; gap: 8px; }
.c-name { font-size: 14px; color: var(--text-1); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.c-unread {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--brand);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  flex-shrink: 0;
}
.c-last {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-empty { padding: 48px 0; text-align: center; font-size: 13px; color: var(--text-3); }

.chat-pane { display: flex; flex-direction: column; min-width: 0; min-height: 0; }
.chat-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-bottom: 1px solid var(--border-light);
}
.h-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
.h-name { font-size: 14px; font-weight: 600; color: var(--text-1); }
.h-sub { font-size: 12px; color: var(--text-3); }
.h-link {
  margin-left: auto;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text-2);
  font-size: 12px;
  height: 28px;
  padding: 0 12px;
  border-radius: 6px;
  cursor: pointer;
}
.h-link:hover { color: var(--brand); border-color: var(--brand); }

.chat-list { flex: 1; overflow-y: auto; padding: 20px; background: #fcfcfd; min-height: 0; }
.msg { margin-bottom: 16px; text-align: left; }
.msg.mine { text-align: right; }
.bubble {
  display: inline-block;
  max-width: 68%;
  padding: 9px 13px;
  border-radius: 8px;
  background: #f4f5f7;
  color: var(--text-1);
  font-size: 14px;
  line-height: 1.55;
  text-align: left;
  word-break: break-word;
}
.msg.mine .bubble { background: var(--brand-light); color: #8c2b48; }
.m-time { margin-top: 5px; font-size: 11px; color: var(--text-3); }
.chat-empty { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 0; color: var(--text-3); font-size: 13px; }

.chat-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 12px 18px;
  border-top: 1px solid var(--border-light);
}
.chat-input :deep(.el-textarea__inner) { font-size: 13px; }

@media (max-width: 860px) {
  .msg-body { grid-template-columns: 1fr; height: auto; }
  .conv-pane { border-right: 0; border-bottom: 1px solid var(--border); max-height: 240px; }
  .chat-pane { min-height: 420px; }
}
</style>
