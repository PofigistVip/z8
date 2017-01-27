package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class TablesView extends Tables {
	public static class CLASS<T extends TablesView> extends Tables.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TablesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TablesView(container);
		}
	}

	public Listbox.CLASS<Listbox> fieldsListbox = new Listbox.CLASS<Listbox>(this);
	private Fields.CLASS<Fields> fields = new Fields.CLASS<Fields>(this);

	public TablesView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		fields.setIndex("fields");

		readOnly = bool.True;
		columnCount = new integer(6);

		fieldsListbox.setIndex("fieldsListbox");
		fieldsListbox.setDisplayName(Fields.displayNames.Title);

		Fields fields = this.fields.get();

		fieldsListbox.get().query = this.fields;
		fieldsListbox.get().link = fields.table;
		fieldsListbox.get().height = new integer(6);
		fieldsListbox.get().sortFields.add(fields.position);

		fields.columns.add(fields.name);
		fields.columns.add(fields.type);
		fields.columns.add(fields.displayName);

		id.get().colspan = new integer(2);

		name.get().colspan = new integer(2);
		name.get().width = new integer(100);

		displayName.get().colspan = new integer(2);
		displayName.get().width = new integer(200);

		description.get().colspan = new integer(6);

		fieldsListbox.get().colspan = new integer(6);

		registerFormField(id);
		registerFormField(name);
		registerFormField(displayName);
		registerFormField(description);
		registerFormField(fieldsListbox);

		nameFields.add(displayName);
		nameFields.add(name);
		sortFields.add(name);

		objects.add(this.fields);
	}
}
