/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jboss.quickstarts.wfk.review.Review;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>This is a the Domain object. The Restaurant class represents how Restaurant resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how a restaurant are retrieved from the database (with @NamedQueries), and acceptable values
 * for Restaurant fields (with @NotNull, @Pattern etc...)<p/>
 *
 * @author Jiaxuan Xu
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Restaurant.FIND_ALL, query = "SELECT c FROM Restaurant c ORDER BY c.phonenumber ASC"),
		@NamedQuery(name = Restaurant.FIND_BY_PHONENUMBER, query = "SELECT c FROM Restaurant c WHERE c.phonenumber = :phonenumber")})
@XmlRootElement
@Table(name = "restaurant", uniqueConstraints = @UniqueConstraint(columnNames = "phonenumber"))
public class Restaurant implements Serializable {
	// /** Default value included to remove warning. Remove or modify at will. **/
	// private static final long serialVersionUID = 2L;

	public static final String FIND_ALL = "Restaurant.findAll";
	public static final String FIND_BY_PHONENUMBER = "Restaurant.findByPhonenumber";

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@NotNull
	@Size(min = 1, max = 50)
	@Pattern(regexp = "[A-Za-z]+", message = "a non-empty alphabetical string less than 50 characters in length")
	private String name;

	@NotNull
	@Pattern(regexp = "^0[0-9]{10}$")
	private String phonenumber;

	@NotNull
	@Pattern(regexp = "^[A-Z0-9]{6}$")
	@Column(name = "postcode")
	private String postcode;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "restaurant")
	private List<Review> reviews;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Restaurant)) return false;
		Restaurant restaurant = (Restaurant) o;
		if (!name.equals(restaurant.name)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return "Restaurant{" + "id=" + id + ", name='" + name + '\'' + ", phonenumber='" + phonenumber + '\'' + ", postcode='" + postcode +
				'\'' + ", reviews=" + reviews + '}';
	}
}
