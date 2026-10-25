# =====================================================================
# XingHai video platform - API regression test script (PowerShell 5.1)
#
# Usage:
#   1) Start backend on 8080:  java -jar backend\target\xinghai-backend.jar
#   2) Reset DB to seed data first (see database dir) for idempotent run
#   3) Run:  powershell -NoExecutionPolicy Bypass -File database\smoke_test.ps1
#
# All green prints:  Summary: PASS 44 / FAIL 0
# NOTE: this script is 100% ASCII on purpose. PowerShell 5.1 reads UTF-8
# without BOM as ANSI(GBK); a Chinese comment right before a code line can
# swallow the newline and merge the code line into the comment, silently
# skipping it. Never add non-ASCII characters to this file.
# JSON bodies are written to temp files and sent with --data-binary @file
# to avoid PowerShell quote mangling.
# =====================================================================
$ErrorActionPreference = "Continue"
$BASE = "http://localhost:8080"

# Credentials are supplied via environment variables; see docs section 13.
$adminUser = $env:XH_ADMIN_USER; $adminPass = $env:XH_ADMIN_PASS
$creatorUser = $env:XH_CREATOR_USER; $creatorPass = $env:XH_CREATOR_PASS
$userUser = $env:XH_USER_USER; $userPass = $env:XH_USER_PASS
if (-not ($adminUser -and $adminPass -and $creatorUser -and $creatorPass -and $userUser -and $userPass)) {
  Write-Host "[FAIL] Missing credentials. Set XH_ADMIN_USER/XH_ADMIN_PASS, XH_CREATOR_USER/XH_CREATOR_PASS, XH_USER_USER/XH_USER_PASS (see docs section 13)."
  exit 1
}
$TMP  = Join-Path $PSScriptRoot ".smoke"
if (-not (Test-Path $TMP)) { New-Item -ItemType Directory -Path $TMP | Out-Null }

$pass = 0; $fail = 0

function J($name, $obj) {
  $json = $obj | ConvertTo-Json -Compress
  $path = Join-Path $TMP $name
  [System.IO.File]::WriteAllText($path, $json, (New-Object System.Text.UTF8Encoding $false))
  return $path
}

function Check($label, $actual, $expected) {
  if ($actual -eq $expected) { Write-Host "[PASS] $label"; $script:pass++ }
  else { Write-Host "[FAIL] $label (expected $expected got $actual)"; $script:fail++ }
}

# ---------- 1. login & auth ----------
$aj = J "admin.json" @{ username = $adminUser; password = $adminPass }
$admin = curl.exe -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" --data-binary "@$aj" | ConvertFrom-Json
Check "Admin login 200" $admin.code 200
$AT = $admin.data.token

$cj = J "creator.json" @{ username = $creatorUser; password = $creatorPass }
$creator = curl.exe -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" --data-binary "@$cj" | ConvertFrom-Json
Check "Creator login 200" $creator.code 200
$CT = $creator.data.token

$uj = J "user.json" @{ username = $userUser; password = $userPass }
$user = curl.exe -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" --data-binary "@$uj" | ConvertFrom-Json
Check "User login 200" $user.code 200
$UT = $user.data.token

$wj = J "wrong.json" @{ username = $userUser; password = "wrongpwd" }
$wrong = curl.exe -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" --data-binary "@$wj" | ConvertFrom-Json
Check "Wrong password -> 1001" $wrong.code 1001

$noToken = curl.exe -s "$BASE/api/user/profile" | ConvertFrom-Json
Check "No token -> 401" $noToken.code 401

# ---------- 2. public apis ----------
$feed = curl.exe -s "$BASE/api/video/feed/recommend?pageNum=1&pageSize=5" | ConvertFrom-Json
Check "Recommend feed 200" $feed.code 200
Check "Feed has videos" ($feed.data.list.Count -gt 0) $true

$detail = curl.exe -s "$BASE/api/video/detail/5" | ConvertFrom-Json
Check "Video detail 200" $detail.code 200

$cats = curl.exe -s "$BASE/api/category/list" | ConvertFrom-Json
Check "Category list 200" $cats.code 200

$tags = curl.exe -s "$BASE/api/tag/list" | ConvertFrom-Json
Check "Tag list 200" $tags.code 200

$notices = curl.exe -s "$BASE/api/notice/list" | ConvertFrom-Json
Check "Notice list 200" $notices.code 200

$ads = curl.exe -s "$BASE/api/advert/list" | ConvertFrom-Json
Check "Advert list 200" $ads.code 200

$lives = curl.exe -s "$BASE/api/live/list" | ConvertFrom-Json
Check "Live list 200" $lives.code 200

$prods = curl.exe -s "$BASE/api/product/list?pageNum=1&pageSize=5" | ConvertFrom-Json
Check "Product list 200" $prods.code 200

$shop = curl.exe -s "$BASE/api/shop/detail/1" | ConvertFrom-Json
Check "Shop detail 200" $shop.code 200

$comments = curl.exe -s "$BASE/api/comment/list/5?pageNum=1&pageSize=5" | ConvertFrom-Json
Check "Comment list 200" $comments.code 200

$search = curl.exe -s "$BASE/api/video/search?keyword=%E5%AE%BF%E8%88%8D&pageNum=1&pageSize=5" | ConvertFrom-Json
Check "Search 200" $search.code 200

$related = curl.exe -s "$BASE/api/video/related/5?limit=3" | ConvertFrom-Json
Check "Related videos 200" $related.code 200
Check "Related has items" ($related.data.Count -gt 0) $true

$userInfo = curl.exe -s "$BASE/api/user/info/1006" | ConvertFrom-Json
Check "Public user info 200" $userInfo.code 200

# ---------- 3. user interactions (with token) ----------
$like1 = curl.exe -s -X POST "$BASE/api/like/video/5" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Like video 200" $like1.code 200
$like2 = curl.exe -s -X POST "$BASE/api/like/video/5" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Repeat like -> 4001" $like2.code 4001

$col1 = curl.exe -s -X POST "$BASE/api/like/collect/5" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Collect video 200" $col1.code 200
$col2 = curl.exe -s -X POST "$BASE/api/like/collect/5" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Repeat collect -> 4002" $col2.code 4002
$cols = curl.exe -s "$BASE/api/like/collections" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "My collections 200" $cols.code 200
$col3 = curl.exe -s -X DELETE "$BASE/api/like/collect/5" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Uncollect 200" $col3.code 200

$f1 = curl.exe -s -X POST "$BASE/api/follow/1004" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Follow creator 200" $f1.code 200
$f2 = curl.exe -s -X POST "$BASE/api/follow/1004" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Repeat follow -> 4101" $f2.code 4101

$cm = J "comment.json" @{ content = "Smoke test comment" }
$c1 = curl.exe -s -X POST "$BASE/api/comment/5" -H "Authorization: Bearer $UT" -H "Content-Type: application/json" --data-binary "@$cm" | ConvertFrom-Json
Check "Post comment 200" $c1.code 200

$msg = J "msg.json" @{ toUserId = 1001; content = "Smoke test message" }
$m1 = curl.exe -s -X POST "$BASE/api/message/send" -H "Authorization: Bearer $UT" -H "Content-Type: application/json" --data-binary "@$msg" | ConvertFrom-Json
Check "Send message 200" $m1.code 200

# ---------- 4. order create & pay ----------
$od = J "order.json" @{ productId = 1; quantity = 1; address = "Smoke Test Address 100" }
$o1 = curl.exe -s -X POST "$BASE/api/order/create" -H "Authorization: Bearer $UT" -H "Content-Type: application/json" --data-binary "@$od" | ConvertFrom-Json
Check "Create order 200" $o1.code 200
$pay = curl.exe -s -X POST "$BASE/api/order/pay/$($o1.data)" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "Pay order 200" $pay.code 200

# ---------- 5. creator ----------
$dsb = curl.exe -s "$BASE/api/creator/dashboard" -H "Authorization: Bearer $CT" | ConvertFrom-Json
Check "Creator dashboard 200" $dsb.code 200

$mine = curl.exe -s "$BASE/api/video/mine?pageNum=1&pageSize=5" -H "Authorization: Bearer $CT" | ConvertFrom-Json
Check "Creator video mine 200" $mine.code 200

$st = J "status.json" @{ status = 1 }
$ps = curl.exe -s -X PUT "$BASE/api/product/status/2" -H "Authorization: Bearer $CT" -H "Content-Type: application/json" --data-binary "@$st" | ConvertFrom-Json
Check "Product on sale 200" $ps.code 200

# Edit own video (ASCII values only; DB is reset after regression anyway)
$ud = J "vupdate.json" @{ title = "Smoke Edit Title"; geoLocation = "SmokeCity" }
$up1 = curl.exe -s -X PUT "$BASE/api/video/1" -H "Authorization: Bearer $CT" -H "Content-Type: application/json" --data-binary "@$ud" | ConvertFrom-Json
Check "Edit own video 200" $up1.code 200

# Cross-user edit must be rejected
$up3 = curl.exe -s -X PUT "$BASE/api/video/1" -H "Authorization: Bearer $UT" -H "Content-Type: application/json" --data-binary "@$ud" | ConvertFrom-Json
Check "Edit others' video -> 403" $up3.code 403

# Publish -> pending -> admin audit pass (full loop)
$pd = J "publish.json" @{
  title = "Smoke Test Video"
  description = "auto test"
  categoryId = 1
  tagIds = @(1, 2)
  videoUrl = "/upload/video/sample1.mp4"
  coverUrl = "/upload/cover/v1.svg"
  duration = 30
  isPublic = 1
  allowComment = 1
  geoLocation = "Zhaoqing"
}
$pv = curl.exe -s -X POST "$BASE/api/video/publish" -H "Authorization: Bearer $CT" -H "Content-Type: application/json" --data-binary "@$pd" | ConvertFrom-Json
Check "Publish video 200" $pv.code 200
$newVid = $pv.data

$ad = J "audit.json" @{ videoId = $newVid; action = "pass" }
$au = curl.exe -s -X POST "$BASE/api/video/audit" -H "Authorization: Bearer $AT" -H "Content-Type: application/json" --data-binary "@$ad" | ConvertFrom-Json
Check "Audit pass 200" $au.code 200

# ---------- 6. admin ----------
$statsA = curl.exe -s "$BASE/api/admin/stats" -H "Authorization: Bearer $AT" | ConvertFrom-Json
Check "Admin stats 200" $statsA.code 200

$usersA = curl.exe -s "$BASE/api/admin/users?pageNum=1&pageSize=5" -H "Authorization: Bearer $AT" | ConvertFrom-Json
Check "Admin users 200" $usersA.code 200

$videosA = curl.exe -s "$BASE/api/video/admin/list?status=1&pageNum=1&pageSize=5" -H "Authorization: Bearer $AT" | ConvertFrom-Json
Check "Admin video list 200" $videosA.code 200

$roles = curl.exe -s "$BASE/api/admin/roles" -H "Authorization: Bearer $AT" | ConvertFrom-Json
Check "Admin roles 200" $roles.code 200

$reports = curl.exe -s "$BASE/api/report/list?pageNum=1&pageSize=5" -H "Authorization: Bearer $AT" | ConvertFrom-Json
Check "Report list 200" $reports.code 200

$rh = J "reporth.json" @{ action = "reject" }
$rh1 = curl.exe -s -X POST "$BASE/api/report/handle/1" -H "Authorization: Bearer $AT" -H "Content-Type: application/json" --data-binary "@$rh" | ConvertFrom-Json
Check "Handle report 200" $rh1.code 200

$blocked = curl.exe -s "$BASE/api/admin/stats" -H "Authorization: Bearer $UT" | ConvertFrom-Json
Check "User blocked from admin -> 403" $blocked.code 403

# =====================================================================
Write-Host "==== Summary: PASS $pass / FAIL $fail ===="
Remove-Item $TMP -Recurse -Force -ErrorAction SilentlyContinue
if ($fail -gt 0) { exit 1 } else { exit 0 }
