from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    ListFlowable,
    ListItem,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


BASE_DIR = Path(__file__).resolve().parent
OUTPUT_PATH = BASE_DIR / "Unique_Finds_Backend_Alignment_Presentation.pdf"


def register_fonts() -> None:
    """Register Microsoft YaHei fonts for Chinese PDF output."""
    pdfmetrics.registerFont(TTFont("YaHei", r"C:\Windows\Fonts\msyh.ttc"))
    pdfmetrics.registerFont(TTFont("YaHeiBold", r"C:\Windows\Fonts\msyhbd.ttc"))


def build_styles():
    """Build reusable paragraph styles for the slide-like PDF."""
    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="CoverTitle",
            fontName="YaHeiBold",
            fontSize=26,
            leading=32,
            textColor=colors.HexColor("#0F172A"),
            alignment=TA_CENTER,
            spaceAfter=14,
        )
    )
    styles.add(
        ParagraphStyle(
            name="CoverSubtitle",
            fontName="YaHei",
            fontSize=12,
            leading=18,
            textColor=colors.HexColor("#334155"),
            alignment=TA_CENTER,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SectionTitle",
            fontName="YaHeiBold",
            fontSize=20,
            leading=24,
            textColor=colors.HexColor("#111827"),
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Body",
            fontName="YaHei",
            fontSize=11,
            leading=17,
            textColor=colors.HexColor("#1F2937"),
        )
    )
    styles.add(
        ParagraphStyle(
            name="BodySmall",
            fontName="YaHei",
            fontSize=9.5,
            leading=14,
            textColor=colors.HexColor("#334155"),
        )
    )
    styles.add(
        ParagraphStyle(
            name="Callout",
            fontName="YaHeiBold",
            fontSize=13,
            leading=18,
            textColor=colors.HexColor("#1D4ED8"),
        )
    )
    styles.add(
        ParagraphStyle(
            name="TableText",
            fontName="YaHei",
            fontSize=9.5,
            leading=13,
            textColor=colors.HexColor("#111827"),
            alignment=TA_LEFT,
        )
    )
    return styles


def on_page(canvas, doc):
    """Render footer elements for every page."""
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#CBD5E1"))
    canvas.line(doc.leftMargin, 0.45 * inch, doc.pagesize[0] - doc.rightMargin, 0.45 * inch)
    canvas.setFont("YaHei", 9)
    canvas.setFillColor(colors.HexColor("#64748B"))
    canvas.drawString(doc.leftMargin, 0.24 * inch, "Unique Finds Backend Alignment Presentation")
    canvas.drawRightString(doc.pagesize[0] - doc.rightMargin, 0.24 * inch, f"Page {doc.page}")
    canvas.restoreState()


def bullet_list(styles, items, style_name="Body", bullet_color="#2563EB", left_indent=18):
    """Build a bullet list block."""
    return ListFlowable(
        [
            ListItem(
                Paragraph(item, styles[style_name]),
                leftIndent=left_indent,
                value="bullet",
            )
            for item in items
        ],
        bulletType="bullet",
        start="circle",
        bulletColor=colors.HexColor(bullet_color),
        leftIndent=0,
        spaceBefore=2,
        spaceAfter=4,
    )


def info_box(styles, title, lines, width=11.6 * inch):
    """Build a highlight box with title and supporting points."""
    content = [Paragraph(f"<b>{title}</b>", styles["Callout"])]
    content.extend(Paragraph(line, styles["Body"]) for line in lines)
    table = Table([[content]], colWidths=[width])
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
                ("BOX", (0, 0), (-1, -1), 0.8, colors.HexColor("#BFDBFE")),
                ("LEFTPADDING", (0, 0), (-1, -1), 12),
                ("RIGHTPADDING", (0, 0), (-1, -1), 12),
                ("TOPPADDING", (0, 0), (-1, -1), 10),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
            ]
        )
    )
    return table


def simple_table(styles, data, widths, header_bg="#DBEAFE"):
    """Build a styled table for slide content."""
    normalized = []
    for row in data:
        normalized.append([Paragraph(cell, styles["TableText"]) for cell in row])
    table = Table(normalized, colWidths=widths, repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor(header_bg)),
                ("FONTNAME", (0, 0), (-1, 0), "YaHeiBold"),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#0F172A")),
                ("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#CBD5E1")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 8),
                ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                ("TOPPADDING", (0, 0), (-1, -1), 7),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
            ]
        )
    )
    return table


def build_story(styles):
    """Compose the full PDF story."""
    story = []

    # Cover
    story.append(Spacer(1, 1.0 * inch))
    story.append(Paragraph("Unique Finds Backend<br/>前后端对齐演示稿", styles["CoverTitle"]))
    story.append(
        Paragraph(
            "面向 5 月联调会议的展示版 PDF<br/>重点说明：当前完成程度、前后端需要对齐的内容、需要共同拍板的规则",
            styles["CoverSubtitle"],
        )
    )
    story.append(Spacer(1, 0.38 * inch))
    story.append(
        info_box(
            styles,
            "这份材料适合怎么讲",
            [
                "先讲后端已完成范围，再讲前端可以立刻开做的页面，最后集中拍板展示规则与联调顺序。",
                "核心目的不是报流水账，而是把可联调范围、产品规则、实现边界一次讲清楚。",
            ],
            width=8.8 * inch,
        )
    )
    story.append(Spacer(1, 0.5 * inch))
    story.append(Paragraph("版本日期：2026-05-10", styles["CoverSubtitle"]))
    story.append(PageBreak())

    # Current completion
    story.append(Paragraph("1. 当前后端完成程度", styles["SectionTitle"]))
    story.append(
        info_box(
            styles,
            "当前状态一句话概括",
            [
                "后端已经不是只有登录和发帖的 demo 版，而是具备账号、帖子、社区互动、搜索、举报和基础审核治理的一套基础社区后端。"
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    data = [
        ["模块", "已经完成的能力"],
        ["账号体系", "注册、密码登录、邮箱验证码登录、JWT 鉴权、当前用户信息"],
        ["用户资料", "查询我的资料、修改昵称/头像/bio"],
        ["帖子能力", "发帖、编辑、删除、详情、已发布列表、我的帖子、多图、浏览量累计"],
        ["社区互动", "评论/回复/删除自己的评论、评论分页、游客可读评论、我的评论、点赞/收藏/互动状态"],
        ["搜索发现", "关键词搜索、分类筛选、latest/hot 排序、列表分页结构统一"],
        ["审核治理", "举报帖子/评论、待审核帖子、管理端处理举报、审核通过/拒绝/隐藏帖子、隐藏/删除评论"],
        ["数据库支撑", "互动 SQL、审核治理 SQL、评论/点赞/收藏计数触发器"],
    ]
    story.append(simple_table(styles, data, [1.4 * inch, 10.0 * inch]))
    story.append(PageBreak())

    # What frontend can do now
    story.append(Paragraph("2. 前端现在可以直接开做什么", styles["SectionTitle"]))
    story.append(
        bullet_list(
            styles,
            [
                "登录/注册/获取当前用户信息页面可以开始联调。",
                "公开帖子列表、帖子详情、搜索结果页可以开始搭建主浏览链路。",
                "发帖、我的帖子、我的资料、我的收藏、我的评论等用户侧页面已经有后端支撑。",
                "评论、点赞、收藏等互动按钮的行为逻辑已经具备接口基础。",
                "如果前端负责管理端，也可以开始做待审核帖子页、举报列表页和基础治理操作页。",
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    story.append(
        info_box(
            styles,
            "给前端的核心结论",
            [
                "现在最适合先做用户主链路页面，而不是一上来重压管理端细节。",
                "第一阶段前端可以优先完成：登录 -> 首页公开帖子 -> 详情 -> 发帖/我的帖子 -> 评论互动。"
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    quick_data = [
        ["页面/能力", "当前是否可联调", "备注"],
        ["登录 / 注册", "是", "建议先接 token 存储与登录态恢复"],
        ["首页公开帖子列表", "是", "游客可看，分页结构已统一"],
        ["帖子详情", "是", "详情里可直接带当前用户互动状态"],
        ["发帖 / 我的帖子", "是", "需要前端展示帖子状态差异"],
        ["评论 / 回复", "是", "需统一删除/隐藏后的展示策略"],
        ["搜索页", "是", "建议第一版先做关键词 + 排序 + 分类"],
        ["举报 / 管理端治理", "是", "适合在主链路跑通后接入"],
    ]
    story.append(simple_table(styles, quick_data, [2.2 * inch, 1.1 * inch, 8.1 * inch], header_bg="#E0F2FE"))
    story.append(PageBreak())

    # Alignment methodology
    story.append(Paragraph("3. 建议的对齐方式", styles["SectionTitle"]))
    story.append(
        info_box(
            styles,
            "推荐采用：按页面驱动，而不是按零散接口驱动",
            [
                "不要上来逐个念接口，而是按前端即将开发的页面来讨论：这个页面谁能看、要调哪些接口、有哪些状态、哪些规则必须一起定。"
            ],
        )
    )
    story.append(Spacer(1, 0.16 * inch))
    method_data = [
        ["推荐流程", "说明"],
        ["先讲范围", "先说明后端完成到哪里、哪些可联调、这次会议要拍板什么"],
        ["按页面走", "登录页、首页列表、详情页、发帖页、我的页、搜索页、举报入口、管理端"],
        ["按统一模板问", "谁能看？要哪些接口？有哪些状态？哪些按钮需要登录？哪些规则今天必须定？"],
        ["最后收口", "明确联调顺序、待补问题和谁负责继续推进"],
    ]
    story.append(simple_table(styles, method_data, [2.0 * inch, 9.4 * inch], header_bg="#EDE9FE"))
    story.append(Spacer(1, 0.16 * inch))
    story.append(Paragraph("页面级对齐建议统一围绕下面 5 个问题：", styles["Callout"]))
    story.append(
        bullet_list(
            styles,
            [
                "这个页面谁能看？游客、登录用户、本人、管理员的权限边界分别是什么？",
                "这个页面需要哪些接口？列表、详情、提交、状态接口是否齐全？",
                "这个页面有哪些核心状态？空状态、未登录、审核中、已删除、被拒绝、被隐藏怎么展示？",
                "这个页面哪些按钮必须登录？未登录点击后前端怎么引导？",
                "这个页面有哪些规则今天必须拍板？否则很容易后面反复改。",
            ],
        )
    )
    story.append(PageBreak())

    # Module alignment details page 1
    story.append(Paragraph("4. 用户侧模块：建议重点对齐的内容", styles["SectionTitle"]))
    module_data_1 = [
        ["模块", "这次需要对齐什么", "建议默认方案"],
        ["登录与鉴权", "token 存储、登录失效处理、游客接口边界、未登录点击互动如何引导", "游客只开放公开只读；未登录点击互动统一引导登录"],
        ["用户资料", "昵称/头像/bio 的展示与编辑方式、字段长度提示、头像是否先走 URL", "第一版头像先按 URL 方案，不把上传系统临时塞进本轮"],
        ["公开帖子列表", "分页交互、列表卡片展示字段、游客可见边界", "统一返回 page/pageSize/total/items；前端先做分页或加载更多"],
        ["帖子详情", "多图展示、作者态与游客态差异、互动按钮状态回显", "直接使用 likedByCurrentUser / favoritedByCurrentUser 渲染按钮"],
        ["发帖 / 我的帖子", "状态提示、编辑后是否重新审核、删除后回跳逻辑", "作者可见审核状态，前端必须显示审核中/被拒绝/已隐藏提示"],
    ]
    story.append(simple_table(styles, module_data_1, [1.6 * inch, 5.4 * inch, 4.4 * inch], header_bg="#DCFCE7"))
    story.append(PageBreak())

    # Module alignment details page 2
    story.append(Paragraph("5. 互动与治理模块：建议重点对齐的内容", styles["SectionTitle"]))
    module_data_2 = [
        ["模块", "这次需要对齐什么", "建议默认方案"],
        ["评论", "一级评论分页方式、回复展示、删除后保留还是消失、隐藏后的前端处理", "DELETED 保留占位；HIDDEN 不展示；游客可读评论但不可发评论"],
        ["点赞 / 收藏", "乐观更新还是等接口成功、未登录点击提示、数量失败回滚", "第一版可以先等接口成功再更新，逻辑更稳"],
        ["搜索", "keyword/categoryId/sort 的交互方式、搜索页第一版范围", "先做关键词 + 分类 + latest/hot，不先做历史记录和联想词"],
        ["举报", "举报原因展示、重复举报提示、成功后的前端反馈文案", "举报原因用固定枚举；提交成功给明确 toast 提示"],
        ["管理端审核", "第一版做多大、审核理由是否必填、帖子审核和举报处理的页面拆分", "先做最小可用版：待审核帖子 + 举报列表 + 处理动作"],
    ]
    story.append(simple_table(styles, module_data_2, [1.6 * inch, 5.4 * inch, 4.4 * inch], header_bg="#FCE7F3"))
    story.append(PageBreak())

    # Must decide
    story.append(Paragraph("6. 这次会议必须拍板的规则", styles["SectionTitle"]))
    story.append(
        info_box(
            styles,
            "这些问题如果不定，后面最容易反复返工",
            [
                "真正会卡联调的，往往不是接口有没有，而是前后端对展示策略和产品规则理解不一致。"
            ],
        )
    )
    story.append(Spacer(1, 0.16 * inch))
    must_decide = [
        ["必须定的事项", "为什么必须这次定"],
        ["游客能力边界", "决定哪些页面和按钮对未登录用户可见，也影响前端权限拦截方式"],
        ["帖子状态展示", "作者看到审核中/被拒绝/已隐藏内容时，页面要给什么提示文案和标记"],
        ["评论删除/隐藏策略", "会直接影响评论树结构、前端渲染逻辑和用户理解"],
        ["分页交互方式", "影响前端列表容器、滚动方案和后端调用节奏"],
        ["图片方案", "如果现在还是 URL 模式，前端就不能按完整文件上传体验去做"],
        ["搜索第一版范围", "决定搜索页先做到多深，避免前端过早设计复杂搜索体验"],
        ["管理端第一版范围", "避免一开始做太重的大后台，拖慢主链路交付"],
    ]
    story.append(simple_table(styles, must_decide, [2.4 * inch, 9.0 * inch], header_bg="#FEE2E2"))
    story.append(PageBreak())

    # Recommended default decisions
    story.append(Paragraph("7. 我建议直接采用的默认方案", styles["SectionTitle"]))
    story.append(
        bullet_list(
            styles,
            [
                "游客只开放公开只读能力：可看公开帖子列表、公开详情、评论；不可评论、点赞、收藏、举报、发帖。",
                "帖子状态展示建议统一：PUBLISHED 正常展示；PENDING_REVIEW 显示审核中；REJECTED 显示未通过审核 + 原因；HIDDEN 显示已隐藏 + 原因；DELETED 不在普通页面展示。",
                "评论状态展示建议统一：VISIBLE 正常展示；DELETED 保留“该评论已删除”的占位；HIDDEN 直接不展示。",
                "分页统一使用 page / pageSize / total / items，前端第一版优先做分页或加载更多，不急着做复杂无限滚动。",
                "图片第一版先按 URL 方案走，上传接口单独作为后续能力补齐。",
                "搜索第一版先收敛为关键词 + 分类 + latest/hot 排序。",
                "管理端先做最小可用版，只保证待审核帖子、举报列表和处理动作跑通。",
            ]
        )
    )
    story.append(Spacer(1, 0.16 * inch))
    story.append(
        info_box(
            styles,
            "为什么这样定比较合理",
            [
                "因为这套默认方案最贴合你们当前后端能力，也最能帮助前端尽快把用户主链路做出来。",
                "先把规则定简单、定清楚，比过早追求完整度更重要。"
            ],
        )
    )
    story.append(PageBreak())

    # Integration order
    story.append(Paragraph("8. 推荐联调顺序", styles["SectionTitle"]))
    order_data = [
        ["优先级", "联调内容", "原因"],
        ["1", "登录 / 注册 / 当前用户", "先把登录态和鉴权打通，后面所有私有页面都依赖它"],
        ["2", "用户资料", "改动面小，能尽快形成“我的页面”基础"],
        ["3", "公开帖子列表", "最适合先搭主浏览链路，且游客也能看"],
        ["4", "帖子详情", "详情页能承接评论、点赞、收藏、举报入口"],
        ["5", "发帖 / 我的帖子", "接入作者侧核心能力，并验证帖子状态展示"],
        ["6", "评论 / 回复", "主社区互动链路，需同步定评论展示规则"],
        ["7", "点赞 / 收藏 / 我的收藏", "在主链路稳定后接入交互增强能力"],
        ["8", "搜索", "有内容浏览链路后再补查找和发现更自然"],
        ["9", "举报", "基础主站跑通后再补治理入口更顺"],
        ["10", "管理端审核", "放在最后做，避免一开始被后台复杂度拖慢整体节奏"],
    ]
    story.append(simple_table(styles, order_data, [0.75 * inch, 2.8 * inch, 7.85 * inch], header_bg="#FEF3C7"))
    story.append(PageBreak())

    # Meeting outcome
    story.append(Paragraph("9. 会议结束前必须产出的结果", styles["SectionTitle"]))
    story.append(
        bullet_list(
            styles,
            [
                "前端先做哪些页面，顺序怎么排。",
                "哪些展示规则已经拍板，后续不再来回改口。",
                "哪些问题暂时不进本轮，比如上传系统、榜单/趋势、AI 搜索等。",
                "哪些同学负责继续推进接口联调、页面实现、规则补充和问题回收。",
            ]
        )
    )
    story.append(Spacer(1, 0.16 * inch))
    story.append(
        info_box(
            styles,
            "结论页可以这样讲",
            [
                "后端主链路已经比较完整，这次对齐最重要的是把前端可以直接开做的部分和必须共同拍板的规则拆开。",
                "接口本身已经不少了，但真正决定联调效率的，是游客边界、评论策略、帖子状态提示、图片方案和管理端范围这几件事。"
            ],
        )
    )
    return story


def main() -> None:
    """Generate the final PDF presentation."""
    register_fonts()
    styles = build_styles()
    page_size = (13.333 * inch, 7.5 * inch)
    doc = BaseDocTemplate(
        str(OUTPUT_PATH),
        pagesize=page_size,
        leftMargin=0.6 * inch,
        rightMargin=0.6 * inch,
        topMargin=0.55 * inch,
        bottomMargin=0.62 * inch,
        title="Unique Finds Backend Alignment Presentation",
        author="OpenAI Codex",
    )
    frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="main")
    template = PageTemplate(id="slides", frames=[frame], onPage=on_page)
    doc.addPageTemplates([template])
    doc.build(build_story(styles))
    print(f"Generated PDF: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
