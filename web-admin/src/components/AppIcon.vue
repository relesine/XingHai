<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="strokeWidth"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <g v-html="content"></g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 },
  strokeWidth: { type: [Number, String], default: 1.6 }
})

const icons = {
  search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
  home: '<path d="M4 10.5 12 3l8 7.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z"/>',
  grid: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 5.5a3.5 3.5 0 0 1 0 5"/><path d="M18 14a6 6 0 0 1 4 6"/>',
  film: '<rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 3v18M17 3v18M3 8h4M17 8h4M3 16h4M17 16h4"/>',
  flag: '<path d="M6 21V4"/><path d="M6 4h12l-2 4 2 4H6"/>',
  megaphone: '<path d="M3 11v2a1 1 0 0 0 1 1h2l9 5V5L6 10H4a1 1 0 0 0-1 1z"/><path d="M18 9a4 4 0 0 1 0 6"/>',
  bell: '<path d="M6 16v-6a6 6 0 0 1 12 0v6l2 2H4z"/><path d="M10 21h4"/>',
  live: '<rect x="3" y="6" width="18" height="12" rx="2"/><path d="M10 10l5 2.5-5 2.5z"/>',
  shield: '<path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/><path d="M9 12l2 2 4-4"/>',
  video: '<rect x="3" y="6" width="12" height="12" rx="2"/><path d="M15 10l6-3v10l-6-3z"/>',
  logout: '<path d="M10 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h5"/><path d="M16 8l4 4-4 4"/><path d="M20 12H9"/>',
  external: '<path d="M14 4h6v6"/><path d="M20 4l-9 9"/><path d="M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5"/>',
  'arrow-up': '<path d="M12 19V5"/><path d="M6 11l6-6 6 6"/>',
  'arrow-down': '<path d="M12 5v14"/><path d="M6 13l6 6 6-6"/>',
  eye: '<path d="M2 12s4-6 10-6 10 6 10 6-4 6-10 6S2 12 2 12z"/><circle cx="12" cy="12" r="2"/>',
  heart: '<path d="M12 20s-7-4.5-7-9.5A4.5 4.5 0 0 1 12 7a4.5 4.5 0 0 1 7 3.5C19 15.5 12 20 12 20z"/>',
  comment: '<path d="M21 12a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 15-4z"/>',
  play: '<path d="M8 5l11 7-11 7z"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  plus: '<path d="M12 5v14"/><path d="M5 12h14"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 0 0-.1-1l2-1.5-2-3.4-2.3 1a7 7 0 0 0-1.7-1L14.5 3h-4l-.4 2.6a7 7 0 0 0-1.7 1l-2.3-1-2 3.4L6.1 11a7 7 0 0 0 0 2l-2 1.5 2 3.4 2.3-1a7 7 0 0 0 1.7 1l.4 2.6h4l.4-2.6a7 7 0 0 0 1.7-1l2.3 1 2-3.4-2-1.5c.1-.3.1-.7.1-1z"/>',
  'chevron-right': '<path d="M9 6l6 6-6 6"/>',
  'chevron-down': '<path d="M6 9l6 6 6-6"/>',
  crown: '<path d="M4 8l4 3 4-5 4 5 4-3-2 10H6z"/>',
  bag: '<path d="M5 8h14l1 12H4z"/><path d="M9 8a3 3 0 0 1 6 0"/>',
  receipt: '<path d="M5 3h14v18l-3-2-2 2-2-2-2 2-2-2-3 2z"/><path d="M9 8h6M9 12h6"/>'
}

const content = computed(() => icons[props.name] || icons.grid)
</script>
