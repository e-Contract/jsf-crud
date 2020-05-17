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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class ABEntity implements Serializable {

    @EmbeddedId
    private ABIdentifier identifier;

    @ManyToOne
    @JoinColumn(name = "name", insertable = false, updatable = false)
    private AEntity a;

    @ManyToOne
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private BEntity b;

    private String description;

    public ABIdentifier getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(ABIdentifier identifier) {
        this.identifier = identifier;
    }

    public AEntity getA() {
        return this.a;
    }

    public void setA(AEntity a) {
        this.a = a;
    }

    public BEntity getB() {
        return this.b;
    }

    public void setB(BEntity b) {
        this.b = b;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.identifier).toHashCode();
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
        ABEntity rhs = (ABEntity) obj;
        return new EqualsBuilder()
                .append(this.identifier, rhs.identifier)
                .isEquals();
    }
}
