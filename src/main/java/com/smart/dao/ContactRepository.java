package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

//hamne is contactrepository ka use showcontact ke handler main kiya hain...

public interface ContactRepository extends JpaRepository<Contact,Integer> {
	
	@Query("from Contact as c where c.user.id =:userId")
    public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pePageable);
	
	/*
	 * pageable Interface = ye ek interface hain jo pagination ki innfo store krta
	 * hain.... aur is interface ke pas do information jayegi 1) current page jiska
	 * variable humane page diya hian 2) contact per page = 5
	 */
	
}
