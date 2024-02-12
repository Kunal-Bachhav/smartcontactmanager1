	package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.aspectj.apache.bcel.util.ClassPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal){
		String userName = principal.getName();
		System.out.println("USER NAME : "+userName);
		User user= userRepository.getUserByUserName(userName);
		System.out.println("USER : "+user);
		model.addAttribute(user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model ,Principal principal) {
		 
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "/normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file,
			Principal principal,
			HttpSession session) {
		
		try {
			String name= principal.getName();
			User user= this.userRepository.getUserByUserName(name);
			
			//processign and uploading the file...
			
			if (file.isEmpty()) 
			{
				System.out.println("file is empty.........");
				contact.setImage("contact2.png");
			}
			else
			{
				//hamne contact ke image ka nam diya 
				contact.setImage(file.getOriginalFilename());
				
				//aur us image ko hamne folder main store kiya 
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("image is uploaded...");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Data "+contact);
			System.out.println("added to database");
			session.setAttribute("message",new Message("your contact is Added !! add more....", "success"));
			
			
		} catch (Exception e) {
			 System.out.println("error : "+e.getMessage());
			 e.printStackTrace();
			 session.setAttribute("message",new Message("Something Went wrong ,,Try again...", "danger"));

		}
		 
		return "/normal/add_contact_form";
	}
	
	
//	show contact handler
// hame per page chahiye 5 contacts.....
	
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model,Principal principal )
	{
	    model.addAttribute("title","show user contact");
	    
	    //yahan pe hame username mila..
	    String userName = principal.getName();
	    
	    //aur hamne userRepository main username ko pass kr diya 
	    //usase hame user mil gaya....
	    User user = this.userRepository.getUserByUserName(userName);
	    
	    //list main hamne contact ki list bheji hain....aur pageable ka object
	    Pageable pageable =  PageRequest.of(page, 5);
	    Page<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId(),pageable);
	    
	    //aur ye list bhej di hamne view page par...
 	    model.addAttribute("contacts",contacts);
 	    model.addAttribute("currentPage",page);
 	    model.addAttribute("totalPages",contacts.getTotalPages());
	    
		return "normal/show_contacts";
	}
	
	//showing perticular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal )
	{
		System.out.println("CID"+cId);
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact= contactOptional.get();
		if (contact != null){
		       model.addAttribute("contact", contact);
		       System.out.println("YAY");
		   }
		   else
		       {
		       model.addAttribute("contact", new User());
		       System.out.println("Bag of d**ks");
		   }
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact( @PathVariable("cid") Integer cId,Model model ,HttpSession session,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact =  contactOptional.get();
		System.out.println("contact "+contact.getcId());
		
		/* contact.setUser(null); */
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		this.contactRepository.delete(contact);
		
		System.out.println("DELETED");
		session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update-form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,Model m) {
		m.addAttribute("title","update Contact");
		
		Contact contact =this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact ,
			@RequestParam("profileImage") MultipartFile file,
			Model model,
			HttpSession session,
			Principal principal) 
	{
		
		try {
			
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if (!file.isEmpty()) 
			{
				
				File  deleteFile = new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile, oldContactDetail.getImage());
				file1.delete();

				
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message",new Message("your contact is updated...","success"));
			
			
		} catch (Exception e) 
		{
			
			 e.printStackTrace();
			 
		}
		
		System.out.println("contact name : "+contact.getName());
		System.out.println("contact ID : "+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","profile page");
		return "normal/profile";
		
	}
	
}


