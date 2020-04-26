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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class CarEntity implements Serializable {

    @Id
    private String numberPlate;

    @ManyToMany
    private List<PersonEntity> drivers;

    public CarEntity() {
        super();
    }

    public CarEntity(String numberPlate) {
        this.numberPlate = numberPlate;
    }

    public List<PersonEntity> getDrivers() {
        return this.drivers;
    }

    public void setDrivers(List<PersonEntity> drivers) {
        this.drivers = drivers;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.numberPlate).toHashCode();
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
        CarEntity rhs = (CarEntity) obj;
        return new EqualsBuilder()
                .append(this.numberPlate, rhs.numberPlate)
                .isEquals();
    }

    @Override
    public String toString() {
        return "car " + this.numberPlate;
    }
}
