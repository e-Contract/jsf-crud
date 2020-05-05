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
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class PersonEntity implements Serializable {

    @Id
    private String name;

    @OneToMany
    private List<CarEntity> cars;

    @OneToOne
    private PersonEntity partner;

    @Embedded
    private Address address;

    @ElementCollection
    private List<String> nickNames;

    public PersonEntity() {
        super();
    }

    public PersonEntity(String name) {
        this.name = name;
    }

    public List<CarEntity> getCars() {
        return this.cars;
    }

    public void setCars(List<CarEntity> cars) {
        this.cars = cars;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonEntity getPartner() {
        return this.partner;
    }

    public void setPartner(PersonEntity partner) {
        this.partner = partner;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getNickNames() {
        return this.nickNames;
    }

    public void setNickNames(List<String> nickNames) {
        this.nickNames = nickNames;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.name).toHashCode();
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
        PersonEntity rhs = (PersonEntity) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .isEquals();
    }
}
