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
package org.jboss.quickstarts.wfk.review;

import org.jboss.quickstarts.wfk.restaurant.Restaurant;
import org.jboss.quickstarts.wfk.user.User;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>This is a the Domain object. The Review class represents how Review resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how a review are retrieved from the database (with @NamedQueries), and acceptable values
 * for Review fields (with @NotNull, @Pattern etc...)<p/>
 *
 * @author Jiaxuan Xu
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Review.FIND_ALL, query = "SELECT c FROM Review c"),
		@NamedQuery(name = Review.FIND_BY_RESTAURANT_ID_AND_USER_ID, query =
				"SELECT c FROM Review c WHERE c.restaurant.id = :restaurantId and c" + ".user.id = :userId"),
		@NamedQuery(name = Review.FIND_ALL_BY_USER_ID, query = "SELECT c FROM Review c WHERE c.user.id = :userId"),
		@NamedQuery(name = Review.FIND_ALL_BY_RESTAURANT_ID, query = "SELECT c FROM Review c WHERE c.restaurant.id = :restaurantId")})
@XmlRootElement
@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}))
public class Review implements Serializable {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "Review.findAll";
	public static final String FIND_BY_RESTAURANT_ID_AND_USER_ID = "Review.findByRestaurantIdAndUserId";
	public static final String FIND_ALL_BY_USER_ID = "Review.findbyUserId";
	public static final String FIND_ALL_BY_RESTAURANT_ID = "Review.findbyRestaurantId";

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@ApiModelProperty(readOnly = true)
	private Long id;

	@Size(min = 0, max = 300, message = " a non-empty string less than 300 characters in length.")
	private String review;

	@Min(0)
	@Max(5)
	private int rating;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;

	// @ApiModelProperty(hidden = true)
	// @JsonIgnore
	// @OneToOne(cascade = CascadeType.ALL, mappedBy = "restaurantReview")
	// private TravelAgentReview travelAgentReview;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}
	//
	// public TravelAgentReview getTravelAgentReview() {
	// 	return travelAgentReview;
	// }
	//
	// public void setTravelAgentReview(TravelAgentReview travelAgentReview) {
	// 	this.travelAgentReview = travelAgentReview;
	// }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Review)) return false;
		Review review = (Review) o;
		if (!user.equals(review.user) || !restaurant.equals(review.restaurant)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = id.intValue();
		result = 31 * result + user.hashCode();
		result = 31 * result + restaurant.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Review{" + "id=" + id + ", review='" + review + '\'' + ", rating=" + rating + ", user=" + user + ", restaurant=" +
				restaurant + '}';
	}
}
