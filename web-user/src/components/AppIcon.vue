<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <g v-html="content"></g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
  strokeWidth: { type: [Number, String], default: 1.6 }
})

const icons = {
  search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
  home: '<path d="M4 10.5 12 3l8 7.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z"/>',
  compass: '<circle cx="12" cy="12" r="9"/><path d="M15.5 8.5l-2 5-5 2 2-5z"/>',
  live: '<rect x="3" y="6" width="18" height="12" rx="2"/><path d="M10 10l5 2.5-5 2.5z"/>',
  shop: '<path d="M5 8h14l1 12H4z"/><path d="M9 8a3 3 0 0 1 6 0"/>',
  upload: '<path d="M12 16V4"/><path d="M7 9l5-5 5 5"/><path d="M4 20h16"/>',
  message: '<path d="M4 5h16v11H9l-5 4z"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  heart: '<path d="M12 20s-7-4.5-7-9.5A4.5 4.5 0 0 1 12 7a4.5 4.5 0 0 1 7 3.5C19 15.5 12 20 12 20z"/>',
  star: '<path d="M12 3l2.7 5.5 6 .9-4.3 4.2 1 6-5.4-2.8-5.4 2.8 1-6L3.3 9.4l6-.9z"/>',
  comment: '<path d="M21 12a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 15-4z"/>',
  share: '<path d="M15 5l6 5-6 5"/><path d="M21 10H8a5 5 0 0 0-5 5v4"/>',
  play: '<path d="M8 5l11 7-11 7z"/>',
  more: '<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>',
  'arrow-left': '<path d="M15 18l-6-6 6-6"/>',
  'arrow-up': '<path d="M12 19V5"/><path d="M6 11l6-6 6 6"/>',
  check: '<path d="M5 12l4 4L19 6"/>',
  close: '<path d="M6 6l12 12"/><path d="M18 6L6 18"/>',
  edit: '<path d="M4 20h4L19 9l-4-4L4 16z"/>',
  trash: '<path d="M4 7h16"/><path d="M9 7V4h6v3"/><path d="M6 7l1 13h10l1-13"/>',
  eye: '<path d="M2 12s4-6 10-6 10 6 10 6-4 6-10 6S2 12 2 12z"/><circle cx="12" cy="12" r="2"/>',
  location: '<path d="M12 21s-6-5-6-10a6 6 0 1 1 12 0c0 5-6 10-6 10z"/><circle cx="12" cy="11" r="2"/>',
  plus: '<path d="M12 5v14"/><path d="M5 12h14"/>',
  fire: '<path d="M12 3c3 4 5 6 5 9a5 5 0 0 1-10 0c0-3 2-5 5-9z"/>',
  crown: '<path d="M4 8l4 3 4-5 4 5 4-3-2 10H6z"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 0 0-.1-1l2-1.5-2-3.4-2.3 1a7 7 0 0 0-1.7-1L14.5 3h-4l-.4 2.6a7 7 0 0 0-1.7 1l-2.3-1-2 3.4L6.1 11a7 7 0 0 0 0 2l-2 1.5 2 3.4 2.3-1a7 7 0 0 0 1.7 1l.4 2.6h4l.4-2.6a7 7 0 0 0 1.7-1l2.3 1 2-3.4-2-1.5c.1-.3.1-.7.1-1z"/>',
  flag: '<path d="M6 21V4"/><path d="M6 4h12l-2 4 2 4H6"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  cart: '<path d="M4 5h2l2 11h10l2-8H7"/><circle cx="9" cy="20" r="1"/><circle cx="17" cy="20" r="1"/>',
  bell: '<path d="M6 16v-6a6 6 0 0 1 12 0v6l2 2H4z"/><path d="M10 21h4"/>',
  grid: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  film: '<rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 3v18M17 3v18M3 8h4M17 8h4M3 16h4M17 16h4"/>',
  bag: '<path d="M5 8h14l1 12H4z"/><path d="M9 8a3 3 0 0 1 6 0"/>',
  receipt: '<path d="M5 3h14v18l-3-2-2 2-2-2-2 2-2-2-3 2z"/><path d="M9 8h6M9 12h6"/>',
  shield: '<path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/><path d="M9 12l2 2 4-4"/>',
  megaphone: '<path d="M3 11v2a1 1 0 0 0 1 1h2l9 5V5L6 10H4a1 1 0 0 0-1 1z"/><path d="M18 9a4 4 0 0 1 0 6"/>',
  video: '<rect x="3" y="6" width="12" height="12" rx="2"/><path d="M15 10l6-3v10l-6-3z"/>',
  logout: '<path d="M10 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h5"/><path d="M16 8l4 4-4 4"/><path d="M20 12H9"/>',
  phone: '<rect x="7" y="2" width="10" height="20" rx="2"/><path d="M11 18h2"/>'
}

const content = computed(() => icons[props.name] || icons.compass)
</script>
