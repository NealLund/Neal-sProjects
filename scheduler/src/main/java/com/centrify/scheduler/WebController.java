package com.centrify.scheduler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import freemarker.template.TemplateException;

//@SpringBootApplication
//@EnableJpaRepositories(basePackageClasses= {Floorplan.class})


@Controller
public class WebController implements WebMvcConfigurer {
	
    @Autowired
    private EmailService emailService;
	
	@Autowired
	private Floorplan fp;
	
	public WebController() {
		
	}
	
	@PostConstruct
	public void init() {
		parseFile("centrify cube map.csv", fp);
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/results").setViewName("results");
	}

	@GetMapping("/")
	public String showForm(PersonForm personForm) {
		return "form";
	}
	
	@GetMapping("/cancel")
	public String showCancel(CancelForm cancelForm) {
		return "cancel";
	}
	
	@RequestMapping("/report")
	public String showReport(Model model) {
		ArrayList<report> reportList = new ArrayList<report>();
		Calendar clearDate = Calendar.getInstance();
		clearDate.clear();
		
		for(Cubicle c : fp.findAll()) {
			// Don't add this one to the list if the date is null or if it has been cleared
			if(c.getDate() != null && c.getBookedDate().compareTo(clearDate) != 0) {
				report r = new report();
				r.setCubeNum(c.getCubeNum());
				r.setBookDate(c.getDate());
				reportList.add(r);
			}
		}
		
		model.addAttribute("report", reportList);
		return "report";
	}
	

	@PostMapping("/")
	public String checkPersonInfo(@Valid@ModelAttribute("personForm") PersonForm personForm, BindingResult bindingResult, Model model) throws FileNotFoundException {
		
		if (bindingResult.hasErrors()) {
			return "form";
		}
		
		String cubeNum = personForm.getCubeNum();
		Date requestedDate = personForm.getScheduleDate();
		Optional<Cubicle> c = fp.findById(cubeNum);
		
		// If cubeNum is null go back to the form and show an error that the cube was not found
		if(requestedDate == null || requestedDate == null && c.isPresent())
			return "form";
		
		//Returns a does not exist page is cube is not in the csv file
		if(!c.isPresent())
			return "DNE";
		
		Cubicle cube = c.get();
		String conflict = tryBook(requestedDate, cube);
		
		
		
		if (conflict == null) {
			cube.setBookedDate(requestedDate);
			
			//Get mail properties
			Properties prop = new Properties();
			String propFileName = "application.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			
			if(inputStream != null) {
				try {
					prop.load(inputStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				throw new FileNotFoundException("property file " + propFileName + " not found!");
			}
			
			// Send them a confirmation email
			Mail mail = new Mail();
	        mail.setFrom(prop.getProperty("spring.mail.username"));
	        mail.setTo(cube.getEmail());
	        mail.setSubject(prop.getProperty("successSubject"));

			Map<String, Object> mailModel = new HashMap<>();
			mailModel.put("cubeNum", cube.getCubeNum());
			mailModel.put("date", cube.getDate());
	        mail.setModel(mailModel);

	        try {
				emailService.sendSimpleMessage(mail, "successEmail.html");
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// Save the update in the database
			fp.save(cube);
			
			// Show the success form
			return "results";
		}
		else{
			personForm.setCubeNum(conflict);
			System.out.println(conflict);
			return "Fail";
		}
	}
	
	@PostMapping("/cancel")
	public String checkCancel(@Valid@ModelAttribute("cancelForm") CancelForm cancelForm, BindingResult bindingResult, Model model) throws FileNotFoundException {
		

		if (bindingResult.hasErrors()) {
			return "cancel";
		}
		
		String cubeNum = cancelForm.getCubeNum();
		Optional<Cubicle> c = fp.findById(cubeNum);

		//Returns a does not exist page is cube is not in the csv file
		if(!c.isPresent())
			return "DNE";
		
		else {
			Cubicle cube = c.get();
			String oldDate = cube.getDate();
			
			//Clears the book date 
			cube.clearBookDate();
			
			//Get mail properties
			Properties prop = new Properties();
			String propFileName = "application.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			
			if(inputStream != null) {
				try {
					prop.load(inputStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				throw new FileNotFoundException("property file " + propFileName + " not found!");
			}
			
			// Send them a confirmation email
			Mail mail = new Mail();
			 mail.setFrom(prop.getProperty("spring.mail.username"));
		     mail.setTo(cube.getEmail());
		     mail.setSubject(prop.getProperty("cancelSubject"));

			Map<String, Object> mailModel = new HashMap<>();
			mailModel.put("cubeNum", cube.getCubeNum());
			mailModel.put("date", oldDate);
	        mail.setModel(mailModel);

	        try {
				emailService.sendSimpleMessage(mail, "cancelEmail.html");
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			// Save the update in the database
			fp.save(cube);
			
			// Show the success form
			return "cancelSuccess";
		}
	}
	
	//Looks to see if there are scheduling conflicts for the given cube on the given date but is using a calendar object
	public String tryBook(Calendar bookedDate, Cubicle cube) {
		String conflict = null;
		
		//Going through the neighbors list and checking for conflicts
		for(int i = 0; i<cube.getNeighborsCount(); i++) {
			Cubicle neighbor = fp.findById(cube.getNeighbor(i)).get();
			if(neighbor.getBookedDate()!= null && neighbor.getBookedDate().get(Calendar.MONTH) == bookedDate.get(Calendar.MONTH) && neighbor.getBookedDate().get(Calendar.YEAR) == bookedDate.get(Calendar.YEAR) && neighbor.getBookedDate().get(Calendar.DAY_OF_MONTH) == bookedDate.get(Calendar.DAY_OF_MONTH)) {
				conflict = neighbor.getCubeNum();
			}
		}
		
		return conflict;
	}
	
	//Looks to see if there are scheduling conflicts for the given cube on the given date
	public String tryBook(Date bookedDate, Cubicle cube) {
			
		//Converting a date to a calendar
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(bookedDate);
			
		return tryBook(calDate, cube);
	}
	

	
    //Takes a csv file as an input and uses the list in order to fill the floorplan 
	//Ordered by who cannot come in at the same time
	public static void parseFile(String inputFile, Floorplan fp){
		String row;
		ArrayList<String> cubeList = new ArrayList<String>();
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//Reading each line of the csv file, each row is a list of neighboring cubes in format cube, email
			while ((row = csvReader.readLine()) != null) {
			    String[] data = row.split(",");
			    cubeList.clear();
			    System.out.println("***** PROCESSING NEIGHBOR CLUSTER *****");
			    for(int i = 0; i<data.length; i+=2) {
			    	System.out.println("Cube number = " + data[i]);
			    	System.out.println("   Email = " + data[i+1]);
			    	Cubicle newCube = new Cubicle(data[i], data[i+1]);
			    	
			    	// See if this cube is already in the database
			    	Optional<Cubicle> existingCube = fp.findById(data[i]);
		            if (!existingCube.isPresent()) {
			    		// Save this cube in the database
			    		fp.save(newCube);
			    		System.out.println("   Adding to database.");
			    		// Get the db copy back
			    	}
			    	else {
			    		System.out.println("   Already in database with "+existingCube.get().getNeighborsCount()+" neighbors.");
			    	}
				    
			    	cubeList.add(data[i]);
			    	
			    	
			    }
			    // Go through all the cubes in this cluster
			    for(int i = 0; i<cubeList.size(); i++) {
		    		Cubicle c = fp.findById(cubeList.get(i)).get();
		    		
		    		System.out.println("Setting neighbors for "+c.getCubeNum());
		    		
		    		List<String> nl = c.getNeighbors();
		    		
		    		// Go through all the cubes and set them as this cubes neighbor
			    	for(int j = 0; j<cubeList.size(); j++) {
			    		
			    		// Get the next cube in the list 
			    		String nextNeighbor = cubeList.get(j);
			    		
			    		// If the cubelist is empty and this cube isn't already in the list, and it's not me add it
			    		if (nl !=null && !nl.contains(nextNeighbor) && nextNeighbor != c.getCubeNum()) {
			    			// Add this node as a number to the cube
			    			c.addNeighbor(nextNeighbor);
			    		}
			    	}
			    	fp.save(c);
			    }
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			csvReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
