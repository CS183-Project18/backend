from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


BASE_DIR = Path(__file__).resolve().parent
OUTPUT_PATH = BASE_DIR / "Unique_Finds_Backend_Alignment_Brief_v3.pdf"


def register_fonts() -> None:
    """Register Chinese fonts for PDF export."""
    pdfmetrics.registerFont(TTFont("YaHei", r"C:\Windows\Fonts\msyh.ttc"))
    pdfmetrics.registerFont(TTFont("YaHeiBold", r"C:\Windows\Fonts\msyhbd.ttc"))


def build_styles():
    """Create paragraph styles used in the brief PDF."""
    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="CoverTitle",
            fontName="YaHeiBold",
            fontSize=24,
            leading=30,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#0F172A"),
            spaceAfter=14,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SectionTitle",
            fontName="YaHeiBold",
            fontSize=19,
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
            leading=19,
            textColor=colors.HexColor("#1F2937"),
            spaceAfter=2,
        )
    )
    styles.add(
        ParagraphStyle(
            name="BulletLine",
            fontName="YaHei",
            fontSize=11,
            leading=20,
            textColor=colors.HexColor("#1F2937"),
            leftIndent=16,
            firstLineIndent=-10,
            spaceBefore=2,
            spaceAfter=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Small",
            fontName="YaHei",
            fontSize=10,
            leading=15,
            textColor=colors.HexColor("#475569"),
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
    return styles


def on_page(canvas, doc):
    """Draw footer on each page."""
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#CBD5E1"))
    canvas.line(doc.leftMargin, 0.42 * inch, doc.pagesize[0] - doc.rightMargin, 0.42 * inch)
    canvas.setFillColor(colors.HexColor("#64748B"))
    canvas.setFont("YaHei", 9)
    canvas.drawString(doc.leftMargin, 0.22 * inch, "Unique Finds Backend Alignment Brief")
    canvas.drawRightString(doc.pagesize[0] - doc.rightMargin, 0.22 * inch, f"Page {doc.page}")
    canvas.restoreState()


def bullets(styles, items):
    """Build a stable bullet-like list with paragraphs."""
    flowables = []
    for item in items:
        flowables.append(Paragraph(f"• {item}", styles["BulletLine"]))
    return flowables


def box(styles, title, lines):
    """Build a highlight box."""
    content = [Paragraph(f"<b>{title}</b>", styles["Callout"])]
    content.extend(Paragraph(line, styles["Body"]) for line in lines)
    table = Table([[content]], colWidths=[11.5 * inch])
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


def simple_table(styles, rows, widths, header_bg):
    """Build a simple styled table."""
    data = [[Paragraph(cell, styles["Body"]) for cell in row] for row in rows]
    table = Table(data, colWidths=widths, repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor(header_bg)),
                ("FONTNAME", (0, 0), (-1, 0), "YaHeiBold"),
                ("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#CBD5E1")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 8),
                ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                ("TOPPADDING", (0, 0), (-1, -1), 7),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
            ]
        )
    )
    return table


def build_story(styles):
    """Compose the brief presentation."""
    story = []

    story.append(Spacer(1, 1.0 * inch))
    story.append(Paragraph("Unique Finds Backend<br/>对齐会议简短版提纲", styles["CoverTitle"]))
    story.append(
        Paragraph(
            "只保留今天真正需要讲的内容：现状、可联调范围、要对齐什么、必须拍板什么",
            styles["Small"],
        )
    )
    story.append(Spacer(1, 0.4 * inch))
    story.append(
        box(
            styles,
            "今天的会议目标",
            [
                "不是汇报写了多少代码，而是讲清楚前端现在能做什么、怎么接、哪些规则必须先统一。",
            ],
        )
    )
    story.append(PageBreak())

    story.append(Paragraph("1. 我今天先讲什么", styles["SectionTitle"]))
    story.extend(
        bullets(
            styles,
            [
                "后端现在已经完成到什么程度。",
                "前端现在可以直接开做哪些页面。",
                "前后端接下来具体要对齐哪些规则。",
                "今天希望现场直接拍板哪些问题。",
            ],
        )
    )
    story.append(Spacer(1, 0.15 * inch))
    story.append(
        box(
            styles,
            "一句话版",
            [
                "今天的重点不是技术细节，而是把页面怎么做、状态怎么显示、权限怎么处理、第一版做到哪里先说清楚。",
            ],
        )
    )
    story.append(Spacer(1, 0.2 * inch))
    story.append(Paragraph("当前后端完成范围可以这样讲：", styles["Callout"]))
    story.extend(
        bullets(
            styles,
            [
                "登录注册、用户资料、帖子主链路已经有了。",
                "评论、点赞、收藏、搜索已经有了。",
                "举报和基础审核治理也已经有了。",
                "所以现在已经可以开始做前端主流程，而不是停留在接口预研阶段。",
            ],
        )
    )
    story.append(PageBreak())

    story.append(Paragraph("2. 前端现在可以直接做什么", styles["SectionTitle"]))
    story.extend(
        bullets(
            styles,
            [
                "登录 / 注册",
                "首页公开帖子列表",
                "帖子详情",
                "发帖 / 我的帖子",
                "评论 / 回复",
                "点赞 / 收藏",
                "搜索",
                "举报",
                "管理端基础审核页",
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    story.append(
        box(
            styles,
            "最适合的联调顺序",
            [
                "先做登录 -> 首页公开帖子 -> 帖子详情 -> 发帖/我的帖子 -> 评论互动。",
                "这样前端能最快把主链路页面跑起来，后面再接搜索、举报和管理端。",
            ],
        )
    )
    story.append(PageBreak())

    story.append(Paragraph("3. 真正要对齐的是什么", styles["SectionTitle"]))
    table_rows = [
        ["要对齐的点", "要讲清楚什么"],
        ["权限边界", "游客能看什么、不能做什么；登录后才能做什么"],
        ["页面展示规则", "审核中/被拒绝/被隐藏的帖子怎么显示；删除/隐藏评论怎么显示"],
        ["分页和列表交互", "前端做分页、加载更多，还是无限滚动"],
        ["图片方案", "现在是图片 URL 还是文件上传"],
        ["搜索第一版范围", "先做关键词/分类/排序，还是做复杂搜索体验"],
        ["管理端第一版范围", "先做最小可用版，还是一开始就做完整后台"],
    ]
    story.append(simple_table(styles, table_rows, [2.4 * inch, 8.8 * inch], "#DBEAFE"))
    story.append(Spacer(1, 0.2 * inch))
    story.append(
        box(
            styles,
            "核心理解",
            [
                "真正会卡住联调的，往往不是接口没有，而是前后端对展示策略和产品规则理解不一样。",
            ],
        )
    )
    story.append(PageBreak())

    story.append(Paragraph("4. 今天最好直接拍板的 6 件事", styles["SectionTitle"]))
    story.extend(
        bullets(
            styles,
            [
                "游客边界：游客可以看什么，不能做什么。",
                "帖子状态展示：审核中、被拒绝、被隐藏分别怎么提示作者。",
                "评论删除 / 隐藏策略：是保留占位，还是直接不显示。",
                "分页怎么做：分页按钮、加载更多，还是无限滚动。",
                "图片现在怎么传：先按 URL，还是已经支持上传。",
                "管理端第一版做多大：最小可用版，还是完整后台。",
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    story.append(
        box(
            styles,
            "推荐默认方案",
            [
                "游客只开放公开只读能力。",
                "评论 DELETED 保留占位，HIDDEN 不展示；帖子状态对作者要明确提示。",
                "管理端先做最小可用版。",
            ],
        )
    )
    story.append(Spacer(1, 0.18 * inch))
    story.append(Paragraph("你最后可以这样收口：", styles["Callout"]))
    story.extend(
        bullets(
            styles,
            [
                "后端主链路已经比较完整，前端现在可以开始做主流程页面。",
                "这次对齐最重要的是把可联调范围和必须统一的规则拆开。",
                "今天先把规则定清楚，后面的联调和开发就会顺很多。",
            ],
        )
    )
    return story


def main() -> None:
    """Generate the brief alignment PDF."""
    register_fonts()
    styles = build_styles()
    page_size = landscape((7.5 * inch, 13.333 * inch))
    doc = BaseDocTemplate(
        str(OUTPUT_PATH),
        pagesize=page_size,
        leftMargin=0.6 * inch,
        rightMargin=0.6 * inch,
        topMargin=0.55 * inch,
        bottomMargin=0.6 * inch,
        title="Unique Finds Backend Alignment Brief",
        author="OpenAI Codex",
    )
    frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="main")
    doc.addPageTemplates([PageTemplate(id="slides", frames=[frame], onPage=on_page)])
    doc.build(build_story(styles))
    print(f"Generated PDF: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
