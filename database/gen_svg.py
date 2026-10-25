# -*- coding: utf-8 -*-
"""生成 XingHai 项目的 SVG 占位封面、头像、店铺图、广告图"""
import os

ROOT = r"F:\AI_full-stack_project\XingHai\uploads"


def svg(path, bg, txt, sub='XingHai', fg='#ffffff'):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    s = (
        '<svg xmlns="http://www.w3.org/2000/svg" width="720" height="1280" viewBox="0 0 720 1280">'
        '<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">'
        '<stop offset="0%%" stop-color="%s"/><stop offset="100%%" stop-color="#1a1a2e"/>'
        '</linearGradient></defs>'
        '<rect width="720" height="1280" fill="url(#g)"/>'
        '<circle cx="600" cy="180" r="90" fill="#ffffff" opacity="0.08"/>'
        '<circle cx="120" cy="1100" r="140" fill="#ffffff" opacity="0.06"/>'
        '<text x="360" y="620" font-family="Microsoft YaHei, sans-serif" font-size="64" fill="%s" '
        'text-anchor="middle" font-weight="bold">%s</text>'
        '<text x="360" y="700" font-family="Microsoft YaHei, sans-serif" font-size="28" fill="%s" '
        'opacity="0.8" text-anchor="middle">%s</text>'
        '<text x="360" y="1230" font-family="Microsoft YaHei, sans-serif" font-size="22" fill="%s" '
        'opacity="0.5" text-anchor="middle">记录每一片星海</text>'
        '</svg>'
    ) % (bg, fg, txt, fg, sub, fg)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(s)


def avatar(path, bg, ch):
    s = (
        '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">'
        '<rect width="200" height="200" rx="100" fill="%s"/>'
        '<circle cx="100" cy="78" r="38" fill="#ffffff" opacity="0.9"/>'
        '<ellipse cx="100" cy="170" rx="62" ry="46" fill="#ffffff" opacity="0.9"/>'
        '<text x="100" y="125" font-family="Microsoft YaHei, sans-serif" font-size="40" fill="%s" '
        'text-anchor="middle" font-weight="bold">%s</text>'
        '</svg>'
    ) % (bg, bg, ch)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(s)


if __name__ == '__main__':
    colors = ['#e74c3c', '#f39c12', '#27ae60', '#2980b9', '#8e44ad', '#16a085', '#d35400',
              '#c0392b', '#2c3e50', '#7f8c8d', '#e67e22', '#3498db', '#1abc9c', '#9b59b6',
              '#f1c40f', '#e84393', '#00b894', '#6c5ce7', '#fd79a8', '#0984e3']
    titles = ['深夜食堂', '广州塔夜景', 'TCP三次握手', 'Python第一个程序', '宿舍日常', '猫咪大战机器人',
              'Java面试经典', '健身房避坑', '程序员的一天', 'Citywalk路线', '翻车现场', '摸鱼小技巧',
              '桌面改造', '通勤随手拍', '爬山日出', '早茶探店', 'Vue3精讲', '弹唱晴天', '游戏高光', '美妆好物']
    for i in range(1, 21):
        svg(os.path.join(ROOT, 'cover', 'v%d.svg' % i), colors[i - 1], titles[i - 1])

    avatars = ['admin'] + ['c%d' % i for i in range(1, 6)] + ['u%d' % i for i in range(1, 11)]
    acolors = ['#2c3e50', '#e74c3c', '#27ae60', '#2980b9', '#8e44ad', '#f39c12', '#16a085',
               '#d35400', '#c0392b', '#3498db', '#1abc9c', '#9b59b6', '#f1c40f', '#e84393',
               '#00b894', '#6c5ce7']
    for i, a in enumerate(avatars):
        avatar(os.path.join(ROOT, 'avatar', '%s.svg' % a), acolors[i % len(acolors)], a[:2].upper())

    for name, c, t in [('shop1', '#e67e22', '星海优选'), ('shop2', '#2980b9', '码农杂货铺')]:
        svg(os.path.join(ROOT, 'cover', '%s.svg' % name), c, t)
    for name, c, t in [('ad1', '#e84393', '早茶点心节'), ('ad2', '#0984e3', '程序员键盘优惠')]:
        svg(os.path.join(ROOT, 'cover', '%s.svg' % name), c, t)
    for i in range(1, 11):
        svg(os.path.join(ROOT, 'cover', 'p%d.svg' % i), colors[(i * 3) % len(colors)], '商品%d' % i)

    total = sum(len(fs) for _, _, fs in os.walk(ROOT))
    print('SVG total:', total)
