package com.socyno.webfwk.menu;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单面板可选项")
public class SystemMenuPaneOption extends SystemMenuPaneSimple implements FieldOption {@Override
    public void setOptionValue(String value) {
        setId(Long.valueOf(value));
    }

    @Override
    public String getOptionValue() {
        return "" + getId();
    }

    @Override
    public String getOptionDisplay() {
        return getName();
    }
}
