package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import de.fau.cs.osr.utils.StringUtils;
import org.sweble.wikitext.engine.Page;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.EntityReferences;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;
import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Pattern;
import org.sweble.wikitext.lazy.parser.Bold;
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.HorizontalRule;
import org.sweble.wikitext.lazy.parser.ImageLink;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.Italics;
import org.sweble.wikitext.lazy.parser.Itemization;
import org.sweble.wikitext.lazy.parser.ItemizationItem;
import org.sweble.wikitext.lazy.parser.MagicWord;
import org.sweble.wikitext.lazy.parser.Paragraph;
import org.sweble.wikitext.lazy.parser.Section;
import org.sweble.wikitext.lazy.parser.Url;
import org.sweble.wikitext.lazy.parser.Whitespace;
import org.sweble.wikitext.lazy.parser.XmlElement;
import org.sweble.wikitext.lazy.preprocessor.TagExtension;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import org.sweble.wikitext.lazy.preprocessor.TemplateParameter;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;


public class MyPlainTextConverter extends AstVisitor {
	private static final Pattern ws = Pattern.compile("\\s+");
	private final SimpleWikiConfiguration config;
	private final int wrapCol;
	private StringBuilder sb;
	private StringBuilder line;
	private boolean pastBod;
	private int needNewlines;
	private boolean needSpace;
	private boolean noWrap;
	private boolean enumerateSections;
	private LinkedList<Integer> sections;

	public MyPlainTextConverter() {
		SimpleWikiConfiguration config = null;

		try {
			config = new SimpleWikiConfiguration("classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
		} catch (IOException var3) {
			var3.printStackTrace();
		} catch (JAXBException var4) {
			var4.printStackTrace();
		}

		this.config = config;
		this.wrapCol = 2147483647;
		this.enumerateSections = false;
	}

	public MyPlainTextConverter(boolean enumerateSection) {
		Object config = null;

		try {
			new SimpleWikiConfiguration("classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
		} catch (IOException var4) {
			var4.printStackTrace();
		} catch (JAXBException var5) {
			var5.printStackTrace();
		}

		this.config = (SimpleWikiConfiguration)config;
		this.wrapCol = 2147483647;
		this.enumerateSections = enumerateSection;
	}

	public MyPlainTextConverter(SimpleWikiConfiguration config, boolean enumerateSections, int wrapCol) {
		this.config = config;
		this.wrapCol = wrapCol;
		this.enumerateSections = enumerateSections;
	}

	protected boolean before(AstNode node) {
		this.sb = new StringBuilder();
		this.line = new StringBuilder();
		this.pastBod = false;
		this.needNewlines = 0;
		this.needSpace = false;
		this.noWrap = false;
		this.sections = new LinkedList();
		return super.before(node);
	}

	protected Object after(AstNode node, Object result) {
		this.finishLine();
		return this.sb.toString();
	}

	public void visit(AstNode n) {
	}

	public void visit(NodeList n) {
		this.iterate(n);
	}

	public void visit(Page p) {
		this.iterate(p.getContent());
	}

	public void visit(Text text) {
		this.write(text.getContent());
		//this.write(" ** ");			// MODIFIED
	}

	public void visit(Whitespace w) {
		this.write(" ");
	}

	public void visit(Bold b) {
		this.iterate(b.getContent());
	}

	public void visit(Italics i) {
		this.iterate(i.getContent());
	}

	public void visit(XmlCharRef cr) {
		this.write(Character.toChars(cr.getCodePoint()));
	}

	public void visit(XmlEntityRef er) {
		String ch = EntityReferences.resolve(er.getName());
		if(ch == null) {
			this.write('&');
			this.write(er.getName());
			this.write(';');
		} else {
			this.write(ch);
		}

	}

	public void visit(Url url) {
		this.write(url.getProtocol());
		this.write(':');
		this.write(url.getPath());
	}

	public void visit(ExternalLink link) {
		//this.write('[');
		this.iterate(link.getTitle());
		//this.write(']');
	}

	public void visit(InternalLink link) {
		try {
			PageTitle e = PageTitle.make(this.config, link.getTarget());
			if(e.getNamespace().equals(this.config.getNamespace("Category"))) {
				return;
			}
		} catch (LinkTargetException var3) {
			;
		}

		this.write(link.getPrefix());

		if(link.getTitle().getContent() != null && !link.getTitle().getContent().isEmpty()) {
			this.write("[[");      // MODIFIED
			this.iterate(link.getTitle());
			this.write("|");      // MODIFIED
			this.write(link.getTarget());
			this.write("]]");      // MODIFIED
		} else {
			this.write("[[");      // MODIFIED
			this.write(link.getTarget());
			this.write("|");
			this.write(link.getTarget());
			this.write("]]");      // MODIFIED
		}
		//}

		this.write(link.getPostfix());

	}

	/*
	public void visit(InternalLink link) {
		try {
			PageTitle e = PageTitle.make(this.config, link.getTarget());
			if(e.getNamespace().equals(this.config.getNamespace("Category"))) {
				return;
			}
		} catch (LinkTargetException var3) {
			;
		}

		this.write("|p|");
		this.write(link.getPrefix());
		this.write("|\\p|");			// MODIFIED

		if(link.getTitle().getContent() != null && !link.getTitle().getContent().isEmpty()) {
			this.write("|t|");			// MODIFIED
			this.iterate(link.getTitle());
			this.write(link.getTitle().toString());
			this.write("|\\t|");			// MODIFIED

		} else {
			this.write("|r|");			// MODIFIED
			this.write(link.getTarget());
			this.write("|\\r|");			// MODIFIED
		}

		this.write("|pp|");			// MODIFIED
		this.write(link.getPostfix());
		this.write("|\\pp|");			// MODIFIED
	}
	*/

	public void visit(Section s) {
		this.finishLine();
		StringBuilder saveSb = this.sb;
		boolean saveNoWrap = this.noWrap;
		this.sb = new StringBuilder();
		this.noWrap = true;
		//this.iterate(s.getTitle());			// MODIFIED
		this.finishLine();
		String title = this.sb.toString().trim();
		this.sb = saveSb;
		if(s.getLevel() >= 1) {
			while(this.sections.size() > s.getLevel()) {
				this.sections.removeLast();
			}

			while(true) {
				if(this.sections.size() >= s.getLevel()) {
					if(this.enumerateSections) {
						StringBuilder sb2 = new StringBuilder();

						for(int i = 0; i < this.sections.size(); ++i) {
							if(i >= 1) {
								sb2.append(this.sections.get(i));
								sb2.append('.');
							}
						}

						if(sb2.length() > 0) {
							sb2.append(' ');
						}

						sb2.append(title);
						title = sb2.toString();
					}
					break;
				}

				this.sections.add(Integer.valueOf(1));
			}
		}

		this.newline(1);
		this.write(title);
		this.newline(1);
		this.noWrap = saveNoWrap;
		this.iterate(s.getBody());

		while(this.sections.size() > s.getLevel()) {
			this.sections.removeLast();
		}

		this.sections.add(Integer.valueOf(((Integer)this.sections.removeLast()).intValue() + 1));
	}

	public void visit(Paragraph p) {
		this.iterate(p.getContent());
		this.newline(1);
	}

	public void visit(HorizontalRule hr) {
		this.newline(1);
	}

	public void visit(XmlElement e) {
		if(e.getName().equalsIgnoreCase("br")) {
			this.newline(1);
		} else {
			this.iterate(e.getBody());
		}

	}

	public void visit(Itemization n) {

		//this.iterate(n.getContent());
	}

	public void visit(ItemizationItem n) {
		this.iterate(n.getContent());
		this.newline(1);
	}

	public void visit(ImageLink n) {
	}

	public void visit(IllegalCodePoint n) {
	}

	public void visit(XmlComment n) {
	}

	public void visit(Template n) {
	}

	public void visit(TemplateArgument n) {
	}

	public void visit(TemplateParameter n) {
	}

	public void visit(TagExtension n) {
	}

	public void visit(MagicWord n) {
	}

	private void newline(int num) {
		if(this.pastBod && num > this.needNewlines) {
			this.needNewlines = num;
		}

	}

	private void wantSpace() {
		if(this.pastBod) {
			this.needSpace = true;
		}

	}

	private void finishLine() {
		this.sb.append(this.line.toString());
		this.line.setLength(0);
	}

	private void writeNewlines(int num) {
		this.finishLine();
		this.sb.append(StringUtils.strrep('\n', num));
		this.needNewlines = 0;
		this.needSpace = false;
	}

	private void writeWord(String s) {
		int length = s.length();
		if(length != 0) {
			if(!this.noWrap && this.needNewlines <= 0) {
				if(this.needSpace) {
					++length;
				}

				if(this.line.length() + length >= this.wrapCol && this.line.length() > 0) {
					this.writeNewlines(1);
				}
			}

			if(this.needSpace && this.needNewlines <= 0) {
				this.line.append(' ');
			}

			if(this.needNewlines > 0) {
				this.writeNewlines(this.needNewlines);
			}

			this.needSpace = false;
			this.pastBod = true;
			this.line.append(s);
		}
	}

	private void write(String s) {
		if(!s.isEmpty()) {
			if(Character.isSpaceChar(s.charAt(0))) {
				this.wantSpace();
			}

			String[] words = ws.split(s);
			int i = 0;

			while(i < words.length) {
				this.writeWord(words[i]);
				++i;
				if(i < words.length) {
					this.wantSpace();
				}
			}

			if(Character.isSpaceChar(s.charAt(s.length() - 1))) {
				this.wantSpace();
			}

		}
	}

	private void write(char[] cs) {
		this.write(String.valueOf(cs));
	}

	private void write(char ch) {
		this.writeWord(String.valueOf(ch));
	}

	private void write(int num) {
		this.writeWord(String.valueOf(num));
	}
}

