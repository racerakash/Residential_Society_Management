/**
 * Notice:- This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Abhijit A. Kulkarni (abhikulkarni.1988@gmail.com).
 */
package com.sms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sms.dto.UserBookingsDTO;
import com.sms.dto.UserDTO;
import com.sms.dto.UserInfoDTO;
import com.sms.entity.AmenitiesBooking;
import com.sms.entity.User;
import com.sms.entity.UserInfo;
import com.sms.exception.CustomGenericException;
import com.sms.security.AuthenticationUserDetailsService;
import com.sms.services.IBookingService;
import com.sms.services.IUserService;
import com.sms.util.SystemConstants;

/**
 * @author Abhijit Kulkarni
 * @since 12 Nov 2016
 * @version 1.0
 */

@Controller
public class UserController extends BaseController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IBookingService bookingService;

	@Autowired(required = true)
	private AuthenticationUserDetailsService userDetailsService;

	@Autowired(required = true)
	@Qualifier("authenticationManager")
	private AuthenticationManager authManager;

	@RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
	ResponseEntity<UserInfoDTO> getUserById(@PathVariable final Long userId) {
		log.debug("********** Retriving user by id ****************");
		try{
			final User user = this.userService.getAuthorisedUserById(userId);
			final List<String> list = new ArrayList<String>();
			list.add(SystemConstants.USER_INFO_DOZER_MAPPER);
			final Mapper mapper = new DozerBeanMapper(list);
			final UserInfoDTO userInfo=new UserInfoDTO();
			mapper.map(user.getUserInfo(), userInfo);
			Hibernate.initialize(user.getUserInfo());
			return new ResponseEntity<UserInfoDTO>(userInfo, null, HttpStatus.OK);
		}catch(final Exception ex){
			log.error(ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	ResponseEntity<Set<User>> getAllUsers() {
		log.debug("********** Retriving all Users ****************");
		final Set<User> users = this.userService.getAllAuthorisedUsers();
		return new ResponseEntity<Set<User>>(users, null, HttpStatus.OK);

	}


	@RequestMapping(value = "/users/authenticate/{userName}/{passWord}",method=RequestMethod.POST)
	public ResponseEntity<UserDTO> authenticate(@PathVariable final String userName, @PathVariable final String passWord) {

		final UsernamePasswordAuthenticationToken authenticationToken
		= new UsernamePasswordAuthenticationToken(userName, passWord);

		Authentication authentication;

		try {
			authentication = this.authManager.authenticate(authenticationToken);
		} catch (final AuthenticationException e) {

			throw new CustomGenericException(401, "The user name or password is incorrect");
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);

		/*
		 * Reload user as password of authentication principal will be null after authorization and
		 * password is needed for token generation
		 */
		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);


		final Map<String, Boolean> roles = this.createRolesMap(userDetails);
		final String token = this.userDetailsService.createToken(userDetails);

		final UserDTO userDTO = new UserDTO(userDetails.getUsername());
		userDTO.setRoles(roles);
		userDTO.setToken(token);

		final User user=this.userDetailsService.getUserDetails(userName);

		userDTO.setUserId(user.getUserId());

		userDTO.setLastLogon(user.getLastUpdateDate());

		userDTO.setFirstName(user.getUserInfo().getPrimFirstName());

		userDTO.setLastName(user.getUserInfo().getPrimLastName());

		userDTO.setUserInfoId(user.getUserInfo().getUserInfoId());

		return new ResponseEntity<UserDTO>(userDTO,HttpStatus.OK);
	}

	private Map<String, Boolean> createRolesMap(final UserDetails userDetails) {
		final Map<String, Boolean> roles = new HashMap<String, Boolean>();
		for (final GrantedAuthority authority : userDetails.getAuthorities()) {
			roles.put(authority.getAuthority(), Boolean.TRUE);
		}

		return roles;
	}

	@RequestMapping(value = "/users/lastLogon/update/{userId}", method = RequestMethod.POST)
	public ResponseEntity<String> updateLastLogOn(@PathVariable final Long userId) {
		log.debug("********** update user last logon date ****************");
		final String userUpdated = this.userService.updateLastLogOn(userId);
		return new ResponseEntity<String>(userUpdated,HttpStatus.OK);
	}

	@RequestMapping(value = "/users/create", method = RequestMethod.POST)
	public ResponseEntity<Void> createUser(@RequestBody final User user) {
		log.debug("********** Create user ****************");
		this.userService.createUser(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/users/userInfo/update", method = RequestMethod.POST)
	@PreAuthorize("hasRole('Member')")
	public ResponseEntity<Void> updateUserInfo(@RequestBody final UserInfo userInfo) {

		try{
			this.userService.updateUserInfo(userInfo);
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(final Exception ex){
			log.error(ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/users/search", method = RequestMethod.POST)
	public @ResponseBody Set<UserInfo> searchUserInfo(@RequestBody final String searchText) {
		return this.userService.searchUserDetails(searchText);
	}

	@RequestMapping(value = "/users/booking", method = RequestMethod.POST)
	public ResponseEntity<Void> saveBookings(@RequestBody final AmenitiesBooking bookingDetails) {
		try{
			this.bookingService.saveBookingDetails(bookingDetails);
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(final Exception ex){
			log.error(ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/users/requests/{userId}", method = RequestMethod.GET)
	public ResponseEntity<List<UserBookingsDTO>> getUsersBookings(@PathVariable final Long userId) {

		List<UserBookingsDTO> bookings=null;
		try{
			final List<AmenitiesBooking> listOfBookings= this.bookingService.getBookingDetails(userId);
			final List<String> list = new ArrayList<String>();
			list.add(SystemConstants.USERS_BOOKINGS);
			final Mapper mapper = new DozerBeanMapper(list);
			bookings=new ArrayList<UserBookingsDTO>();
			mapper.map(listOfBookings, bookings);
		}catch(final Exception ex){
			log.error(ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(bookings,HttpStatus.OK);

	}

}
