/**
 * Notice:- This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Abhijit A. Kulkarni (abhikulkarni.1988@gmail.com).
 */

package com.sms.dao;

import java.util.List;

import com.sms.entity.Amenity;

/**
 * @author Abhijit A. Kulkarni
 * @Crated on Nov 12, 2016
 * @Version 1.0
 */
public interface IAmenitiesJpaDao {

	List<Amenity> getAllAmenities();
}