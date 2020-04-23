/*
 * JSF CRUD project.
 * Copyright (C) 2020 e-Contract.be BV.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see
 * http://www.gnu.org/licenses/.
 */
package be.e_contract.crud.jsf.demo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class AutoIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String description;

    private long amount;

    private boolean checked;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIME)
    private Date timeOnly;

    @Temporal(TemporalType.DATE)
    @Column(updatable = false)
    private Date dateOnly;

    @Enumerated(EnumType.STRING)
    private MyEnum myEnum;

    @ManyToOne
    private DemoEntity demo;

    private Boolean threeValuedLogic;

    @Basic(fetch = FetchType.LAZY)
    @Column(length = 10)
    private String lazyString;

    @Temporal(TemporalType.DATE)
    private Calendar dateOfBirth;

    public AutoIdEntity() {
        super();
    }

    public AutoIdEntity(String description) {
        this.description = description;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getTimeOnly() {
        return this.timeOnly;
    }

    public void setTimeOnly(Date timeOnly) {
        this.timeOnly = timeOnly;
    }

    public Date getDateOnly() {
        return this.dateOnly;
    }

    public void setDateOnly(Date dateOnly) {
        this.dateOnly = dateOnly;
    }

    public MyEnum getMyEnum() {
        return this.myEnum;
    }

    public void setMyEnum(MyEnum myEnum) {
        this.myEnum = myEnum;
    }

    public DemoEntity getDemo() {
        return this.demo;
    }

    public void setDemo(DemoEntity demo) {
        this.demo = demo;
    }

    public Boolean getThreeValuedLogic() {
        return this.threeValuedLogic;
    }

    public void setThreeValuedLogic(Boolean threeValuedLogic) {
        this.threeValuedLogic = threeValuedLogic;
    }

    public String getLazyString() {
        return this.lazyString;
    }

    public void setLazyString(String lazyString) {
        this.lazyString = lazyString;
    }

    public Calendar getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(Calendar dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMyProperty() {
        return "My property " + this.id;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        AutoIdEntity rhs = (AutoIdEntity) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }
}
