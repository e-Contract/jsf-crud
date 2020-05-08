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
import java.math.BigInteger;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@IdClass(DemoIdClass.class)
public class IdClassEntity implements Serializable {

    @Id
    private String name;

    @Id
    @ManyToOne
    private DemoEntity demo;

    private String description;

    private BigInteger bigInteger;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DemoEntity getDemo() {
        return this.demo;
    }

    public void setDemo(DemoEntity demo) {
        this.demo = demo;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getBigInteger() {
        return this.bigInteger;
    }

    public void setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.name).append(this.demo).toHashCode();
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
        IdClassEntity rhs = (IdClassEntity) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .append(this.demo, rhs.demo)
                .isEquals();
    }

    @Override
    public String toString() {
        if (null != this.demo) {
            return this.name + "-" + this.demo.getName();
        }
        return "";
    }
}
