package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.UserRoles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class UsersView extends Users {
	public static class CLASS<T extends UsersView> extends Users.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(UsersView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new UsersView(container);
		}
	}

	public Listbox.CLASS<Listbox> entriesListbox = new Listbox.CLASS<Listbox>(this);
	public Listbox.CLASS<Listbox> rolesListbox = new Listbox.CLASS<Listbox>(this);

	private UserEntries.CLASS<UserEntries> userEntries = new UserEntries.CLASS<UserEntries>(this);
	private UserRoles.CLASS<UserRoles> userRoles = new UserRoles.CLASS<UserRoles>(this);
	
	public UsersView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		userEntries.setIndex("userEntries");
		userRoles.setIndex("userRoles");

		columnCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		entriesListbox.setIndex("entriesListbox");
		entriesListbox.setDisplayName(UserEntries.displayNames.Title);

		rolesListbox.setIndex("rolesListbox");
		rolesListbox.setDisplayName(UserRoles.displayNames.Title);

		UserEntries userEntries = this.userEntries.get();

		entriesListbox.get().query = this.userEntries;
		entriesListbox.get().link = userEntries.user;

		userEntries.position.get().editable = bool.True;

		userEntries.columns.add(userEntries.entries.get().name);
		userEntries.columns.add(userEntries.position);
		userEntries.sortFields.add(userEntries.position);

		UserRoles userRoles = this.userRoles.get();

		rolesListbox.get().query = this.userRoles;
		rolesListbox.get().link = userRoles.user;
		rolesListbox.get().source = new Roles.CLASS<Roles>(this);

		userRoles.columns.add(userRoles.roles.get().name);

		name.get().colspan = new integer(3);
		lastName.get().colspan = new integer(3);
		firstName.get().colspan = new integer(3);
		middleName.get().colspan = new integer(3);

		phone.get().colspan = new integer(3);
		email.get().colspan = new integer(3);
		banned.get().colspan = new integer(3);
		banned.get().setIcon("fa-ban");

		description.get().colspan = new integer(12);

		entriesListbox.get().colspan = new integer(6);
		rolesListbox.get().colspan = new integer(6);

		registerFormField(name);
		registerFormField(lastName);
		registerFormField(firstName);
		registerFormField(middleName);
		registerFormField(phone);
		registerFormField(email);
		registerFormField(banned);
		registerFormField(description);
		registerFormField(rolesListbox);
		registerFormField(entriesListbox);

		sortFields.add(name);

		objects.add(this.userEntries);
		objects.add(this.userRoles);
	}
}
