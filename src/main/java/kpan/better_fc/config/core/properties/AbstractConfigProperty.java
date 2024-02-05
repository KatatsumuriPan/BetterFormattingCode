package kpan.better_fc.config.core.properties;

import com.google.common.base.Splitter;
import kpan.better_fc.config.core.IConfigElement;
import kpan.better_fc.config.core.ModConfigCategory;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class AbstractConfigProperty implements IConfigElement {

	private final String name;
	private final String comment;
	private final int order;

	private boolean isReadValue = false;
	protected boolean requiresWorldRestart = false;
	protected boolean showInGui = true;
	protected boolean requiresMcRestart = false;
	protected boolean dirty = false;

	protected AbstractConfigProperty(String name, String comment, int order) {
		this.name = name;
		this.comment = comment;
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}
	public void resetDirty() {
		dirty = false;
	}
	public abstract boolean readValue(String value);
	public abstract String getAdditionalComment();
	public abstract String getTypeString();
	public abstract String getValueString();
	public abstract String getDefaultValueString();
	public abstract boolean isDefault();
	public abstract void setToDefault();
	public String getLanguageKey() { return getName(); }
	public boolean requiresWorldRestart() {
		return requiresWorldRestart;
	}
	public boolean requiresMcRestart() {
		return requiresMcRestart;
	}
	@Override
	public boolean showInGui() {
		return showInGui;
	}
	public abstract boolean isValidValue(String str);

	@Override
	public void write(BufferedWriter out, int indent) throws IOException {
		String pad0 = ModConfigCategory.getIndent(indent);

		if (!getComment().isEmpty() || !getAdditionalComment().isEmpty()) {

			Splitter splitter = Splitter.onPattern("\r?\n");
			if (!getComment().isEmpty()) {
				for (String commentLine : splitter.split(getComment())) {
					ModConfigCategory.writeLine(out, pad0, "# ", commentLine);
				}
			}
			if (!getAdditionalComment().isEmpty()) {
				for (String commentLine : splitter.split(getAdditionalComment())) {
					ModConfigCategory.writeLine(out, pad0, "# ", commentLine);
				}
			}
		}

		String propName = getName();
		if (!ModConfigCategory.allowedProperties.matchesAllOf(propName)) {
			propName = '"' + propName + '"';
		}

		if (this instanceof AbstractConfigPropertyList list) {
			String pad1 = ModConfigCategory.getIndent(indent + 1);
			ModConfigCategory.writeLine(out, pad0, getTypeString(), ":", propName, " <");

			for (String line : list.getStringValues()) {
				ModConfigCategory.writeLine(out, pad1, line);
			}

			ModConfigCategory.writeLine(out, pad0, " >");
		} else {
			ModConfigCategory.writeLine(out, pad0, getTypeString(), ":", propName, "=", getValueString());
		}
	}
	public static abstract class AbstractConfigPropertyList extends AbstractConfigProperty {

		protected boolean isListLengthFixed = false;
		protected int maxListLength = -1;
		protected AbstractConfigPropertyList(String name, String comment, int order) {
			super(name, comment, order);
		}
		public abstract String[] getStringValues();
	}

}
