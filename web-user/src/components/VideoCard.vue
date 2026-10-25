<template>
    <article class="video-card" @click="open">
        <div class="cover-wrap">
            <img :src="video.coverUrl" :alt="video.title" class="cover" loading="lazy" />
            <span v-if="duration" class="duration">{{ duration }}</span>
            <span class="views">
                <AppIcon name="play" :size="12" :stroke-width="2" />
                {{ formatNum(video.viewCount) }}
            </span>
        </div>
        <div class="info">
            <h3 class="title clamp-2">{{ video.title }}</h3>
            <div class="meta">
                <span class="author">
                    <img :src="video.authorAvatar || '/upload/avatar/u1.svg'" alt="" class="avatar" />
                    {{ video.authorName }}
                </span>
                <span class="likes">
                    <AppIcon name="heart" :size="13" />
                    {{ formatNum(video.likeCount) }}
                </span>
            </div>
        </div>
    </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'

const props = defineProps({
    video: { type: Object, required: true }
})
const router = useRouter()

const duration = computed(() => {
    const s = props.video.duration
    if (!s) return ''
    const m = Math.floor(s / 60)
    const sec = s % 60
    return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})

function formatNum(n) {
    if (!n) return 0
    if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
    if (n >= 10000) return (n / 10000).toFixed(1) + '万'
    return n
}

function open() {
    router.push(`/video/${props.video.id}`)
}
</script>

<style scoped>
.video-card {
    cursor: pointer;
    min-width: 0;
}

.cover-wrap {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background: #eef0f2;
}

.cover {
    width: 100%;
    aspect-ratio: 3 / 4;
    object-fit: cover;
    transition: opacity 0.2s;
}

.video-card:hover .cover {
    opacity: 0.92;
}

.duration {
    position: absolute;
    right: 6px;
    bottom: 6px;
    padding: 1px 5px;
    font-size: 11px;
    line-height: 16px;
    color: #fff;
    background: rgba(0, 0, 0, 0.55);
    border-radius: 3px;
}

.views {
    position: absolute;
    left: 6px;
    bottom: 6px;
    display: flex;
    align-items: center;
    gap: 3px;
    padding: 1px 5px;
    font-size: 11px;
    line-height: 16px;
    color: #fff;
    background: rgba(0, 0, 0, 0.55);
    border-radius: 3px;
}

.info {
    padding: 8px 2px 0;
}

.title {
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    color: var(--text-1);
    min-height: 40px;
}

.video-card:hover .title {
    color: var(--brand);
}

.meta {
    display: flex;
    align-items: center;
    margin-top: 6px;
    font-size: 12px;
    color: var(--text-3);
}

.author {
    display: flex;
    align-items: center;
    gap: 4px;
    min-width: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.avatar {
    width: 18px;
    height: 18px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.likes {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 2px;
    flex-shrink: 0;
}
</style>
