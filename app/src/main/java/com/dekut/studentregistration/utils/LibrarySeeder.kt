package com.dekut.studentregistration.utils

import com.dekut.studentregistration.models.Book

object LibrarySeeder {

    val DEPARTMENTS = listOf(
        "School of Computer Science and IT",
        "School of Engineering",
        "School of Business, Management and Economics",
        "School of Science",
        "Institute of Food and Bioresource Technology",
        "Institute of Tourism and Hospitality Management",
        "Institute of Criminology, Forensics and Security Studies",
        "Institute of Geomatics, GIS and Remote Sensing",
        "School of Nursing"
    )

    fun getSampleBooks(): List<Book> {
        return listOf(
            // ─── 1. School of Computer Science and IT (10 Books) ───
            Book(
                id = "cs_9781292405469",
                isbn = "9781292405469",
                title = "Computer Networking: A Top-Down Approach",
                author = "James Kurose & Keith Ross",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9781292374062",
                isbn = "9781292374062",
                title = "Computer Networks",
                author = "Andrew S. Tanenbaum & David Wetherall",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9781292401113",
                isbn = "9781292401113",
                title = "Artificial Intelligence: A Modern Approach",
                author = "Stuart Russell & Peter Norvig",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9781292097619",
                isbn = "9781292097619",
                title = "Fundamentals of Database Systems",
                author = "Ramez Elmasri & Shamkant Navathe",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9781292061184",
                isbn = "9781292061184",
                title = "Database Systems: A Practical Approach to Design, Implementation, and Management",
                author = "Thomas Connolly & Carolyn Begg",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9780133943030",
                isbn = "9780133943030",
                title = "Software Engineering",
                author = "Ian Sommerville",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9780134670959",
                isbn = "9780134670959",
                title = "Operating Systems: Internals and Design Principles",
                author = "William Stallings",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9780273769224",
                isbn = "9780273769224",
                title = "Introduction to Data Mining",
                author = "Pang-Ning Tan et al.",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9781119547259",
                isbn = "9781119547259",
                title = "Interaction Design: Beyond Human-Computer Interaction",
                author = "Jenny Preece, Helen Sharp & Yvonne Rogers",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "cs_9780134633282",
                isbn = "9780134633282",
                title = "Business Intelligence, Analytics, and Data Science",
                author = "Ramesh Sharda et al.",
                department = "School of Computer Science and IT",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 2. School of Engineering (10 Books) ───
            Book(
                id = "eng_9780073398174",
                isbn = "9780073398174",
                title = "Engineering Mechanics: Statics",
                author = "R. C. Hibbeler",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780133918922",
                isbn = "9780133918922",
                title = "Engineering Mechanics: Dynamics",
                author = "R. C. Hibbeler",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780133915380",
                isbn = "9780133915380",
                title = "Mechanics of Materials",
                author = "R. C. Hibbeler",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780073398005",
                isbn = "9780073398005",
                title = "Fundamentals of Electric Circuits",
                author = "Charles Alexander & Matthew Sadiku",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780134484143",
                isbn = "9780134484143",
                title = "Electric Machinery Fundamentals",
                author = "Stephen Chapman",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780134480515",
                isbn = "9780134480515",
                title = "Electrical Engineering: Principles and Applications",
                author = "Allan R. Hambley",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780133485966",
                isbn = "9780133485966",
                title = "Thermodynamics: An Engineering Approach",
                author = "Yunus Çengel & Michael Boles",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780134444330",
                isbn = "9780134444330",
                title = "Fluid Mechanics: Fundamentals and Applications",
                author = "Yunus Çengel & John Cimbala",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780132858033",
                isbn = "9780132858033",
                title = "Shigley's Mechanical Engineering Design",
                author = "Richard Budynas & J. Keith Nisbett",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "eng_9780073398143",
                isbn = "9780073398143",
                title = "Manufacturing Engineering and Technology",
                author = "Serope Kalpakjian & Steven Schmid",
                department = "School of Engineering",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 3. School of Business, Management and Economics (10 Books) ───
            Book(
                id = "bus_9780134729329",
                isbn = "9780134729329",
                title = "Financial Accounting",
                author = "Jerry Weygandt, Paul Kimmel & Donald Kieso",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9781292211136",
                isbn = "9781292211136",
                title = "Managerial Accounting",
                author = "Ray Garrison, Eric Noreen & Peter Brewer",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9780134729534_mkt",
                isbn = "9780134729534",
                title = "Marketing Management",
                author = "Philip Kotler & Kevin Keller",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9781292400963",
                isbn = "9781292400963",
                title = "Principles of Marketing",
                author = "Philip Kotler & Gary Armstrong",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9781260565553",
                isbn = "9781260565553",
                title = "Management",
                author = "Stephen Robbins & Mary Coulter",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9780134891779",
                isbn = "9780134891779",
                title = "Organizational Behavior",
                author = "Stephen Robbins & Timothy Judge",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9780135183874",
                isbn = "9780135183874",
                title = "Business Statistics",
                author = "Ken Black",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9780134685991",
                isbn = "9780134685991",
                title = "Corporate Finance",
                author = "Stephen Ross, Randolph Westerfield & Jeffrey Jaffe",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9781292402974",
                isbn = "9781292402974",
                title = "Operations Management",
                author = "Nigel Slack & Alistair Brandon-Jones",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "bus_9780134729534_ent",
                isbn = "9780134729534",
                title = "Entrepreneurship: Successfully Launching New Ventures",
                author = "Bruce Barringer & R. Duane Ireland",
                department = "School of Business, Management and Economics",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 4. School of Science (10 Books) ───
            Book(
                id = "sci_9780134763644",
                isbn = "9780134763644",
                title = "Calculus: Early Transcendentals",
                author = "James Stewart",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780134693162",
                isbn = "9780134693162",
                title = "Elementary Linear Algebra",
                author = "Howard Anton & Chris Rorres",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780321982384",
                isbn = "9780321982384",
                title = "Introduction to Probability and Statistics",
                author = "William Mendenhall et al.",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9781292161364",
                isbn = "9781292161364",
                title = "Discrete Mathematics and Its Applications",
                author = "Kenneth Rosen",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780134093413_num",
                isbn = "9780134093413",
                title = "Numerical Analysis",
                author = "Richard Burden & J. Douglas Faires",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9781292027111",
                isbn = "9781292027111",
                title = "Chemistry: The Central Science",
                author = "Theodore Brown et al.",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780321972019",
                isbn = "9780321972019",
                title = "Organic Chemistry",
                author = "Paula Bruice",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780134093413_phy",
                isbn = "9780134093413",
                title = "Fundamentals of Physics",
                author = "David Halliday, Robert Resnick & Jearl Walker",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9781319050740",
                isbn = "9781319050740",
                title = "Biology: How Life Works",
                author = "James Morris et al.",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "sci_9780134580999",
                isbn = "9780134580999",
                title = "Probability and Statistics for Engineers and Scientists",
                author = "Ronald Walpole et al.",
                department = "School of Science",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 5. Institute of Food and Bioresource Technology (10 Books) ───
            Book(
                id = "food_9780471394623",
                isbn = "9780471394623",
                title = "Food Chemistry",
                author = "H.-D. Belitz, W. Grosch & P. Schieberle",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780470678673_proc",
                isbn = "9780470678673",
                title = "Food Processing Technology",
                author = "P. J. Fellows",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780123847300",
                isbn = "9780123847300",
                title = "Food Microbiology: Fundamentals and Frontiers",
                author = "Michael Doyle & Robert Buchanan",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780471142914",
                isbn = "9780471142914",
                title = "Food Analysis",
                author = "S. Suzanne Nielsen",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780123849472",
                isbn = "9780123849472",
                title = "Introduction to Food Engineering",
                author = "R. Paul Singh & Dennis Heldman",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780470678673_pres",
                isbn = "9780470678673",
                title = "Food Preservation Techniques",
                author = "Peter Zeuthen & Leif Bøgh-Sørensen",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780134892083",
                isbn = "9780134892083",
                title = "Nutrition: Concepts and Controversies",
                author = "Frances Sizer & Ellie Whitney",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9781284100052",
                isbn = "9781284100052",
                title = "Understanding Nutrition",
                author = "Eleanor Whitney & Sharon Rolfes",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780128006056",
                isbn = "9780128006056",
                title = "Food Safety Management",
                author = "Yasmine Motarjemi & Huub Lelieveld",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "food_9780470421924",
                isbn = "9780470421924",
                title = "Handbook of Food Science, Technology, and Engineering",
                author = "Y. H. Hui",
                department = "Institute of Food and Bioresource Technology",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 6. Institute of Tourism and Hospitality Management (10 Books) ───
            Book(
                id = "tour_9781292020432_prac",
                isbn = "9781292020432",
                title = "Tourism: Principles and Practice",
                author = "Chris Cooper et al.",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9781292020432_mgmt",
                isbn = "9781292020432",
                title = "Tourism Management",
                author = "Stephen Page",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780132786675",
                isbn = "9780132786675",
                title = "Hospitality Management",
                author = "John R. Walker",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780470088586",
                isbn = "9780470088586",
                title = "Introduction to Hospitality",
                author = "John R. Walker",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780134484488_mkt",
                isbn = "9780134484488",
                title = "Marketing for Hospitality and Tourism",
                author = "Philip Kotler et al.",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780134484488_hosp",
                isbn = "9780134484488",
                title = "Hospitality Marketing Management",
                author = "Robert Reid & David Bojanic",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780750666854",
                isbn = "9780750666854",
                title = "The Business of Hotels",
                author = "Richard Kotas et al.",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780133067407",
                isbn = "9780133067407",
                title = "Food and Beverage Management",
                author = "John Cousins et al.",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780415632534",
                isbn = "9780415632534",
                title = "The Tourism System",
                author = "Robert Christie Mill & Alastair Morrison",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "tour_9780750664782",
                isbn = "9780750664782",
                title = "Event Management: An Introduction",
                author = "Charles Bladen et al.",
                department = "Institute of Tourism and Hospitality Management",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 7. Institute of Criminology, Forensics and Security Studies (10 Books) ───
            Book(
                id = "crim_9780134558516_core",
                isbn = "9780134558516",
                title = "Criminology: The Core",
                author = "Larry Siegel",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9781337557711",
                isbn = "9781337557711",
                title = "Criminology: A Sociological Understanding",
                author = "Steven Barkan",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9781506388460_inv",
                isbn = "9781506388460",
                title = "Criminal Investigation",
                author = "Michael Lyman",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558981_crim",
                isbn = "9780134558981",
                title = "Criminalistics: An Introduction to Forensic Science",
                author = "Richard Saferstein",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558981_intro",
                isbn = "9780134558981",
                title = "Forensic Science: An Introduction",
                author = "Richard Saferstein",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558516_just",
                isbn = "9780134558516",
                title = "Introduction to Criminal Justice",
                author = "Larry Siegel",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9781506388460_art",
                isbn = "9781506388460",
                title = "Criminal Investigation: The Art and the Science",
                author = "Michael Lyman",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558981_fund",
                isbn = "9780134558981",
                title = "Forensic Science: Fundamentals and Investigations",
                author = "Anthony Bertino",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558516_sec",
                isbn = "9780134558516",
                title = "Security Management",
                author = "John Fay",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "crim_9780134558516_info",
                isbn = "9780134558516",
                title = "Principles of Information Security",
                author = "Michael Whitman & Herbert Mattord",
                department = "Institute of Criminology, Forensics and Security Studies",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 8. Institute of Geomatics, GIS and Remote Sensing (10 Books) ───
            Book(
                id = "gis_9781118676951_sci",
                isbn = "9781118676951",
                title = "Geographic Information Systems and Science",
                author = "Paul Longley et al.",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780131495029_interp",
                isbn = "9780131495029",
                title = "Remote Sensing and Image Interpretation",
                author = "Thomas Lillesand, Ralph Kiefer & Jonathan Chipman",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780471958160",
                isbn = "9780471958160",
                title = "Introduction to Geographic Information Systems",
                author = "Kang-tsung Chang",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780471697282_surv",
                isbn = "9780471697282",
                title = "Surveying: Principles and Applications",
                author = "Barry Kavanagh & Tom Mastin",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780133869425",
                isbn = "9780133869425",
                title = "Elementary Surveying: An Introduction to Geomatics",
                author = "Charles Ghilani & Paul Wolf",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780471329351",
                isbn = "9780471329351",
                title = "Geodesy",
                author = "Wolfgang Torge",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780415301676",
                isbn = "9780415301676",
                title = "Photogrammetry: Geometry from Images and Laser Scans",
                author = "Karl Kraus",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9781118676951_fund",
                isbn = "9781118676951",
                title = "GIS Fundamentals: A First Text on Geographic Information Systems",
                author = "Paul Bolstad",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780131495029_prin",
                isbn = "9780131495029",
                title = "Principles of Geographic Information Systems",
                author = "Peter Burrough & Rachael McDonnell",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "gis_9780471697282_them",
                isbn = "9780471697282",
                title = "Thematic Cartography and Geovisualization",
                author = "Terry Slocum et al.",
                department = "Institute of Geomatics, GIS and Remote Sensing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),

            // ─── 9. School of Nursing (10 Books) ───
            Book(
                id = "nurs_9780323810341_fund",
                isbn = "9780323810341",
                title = "Fundamentals of Nursing",
                author = "Patricia Potter et al.",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_med",
                isbn = "9780323810341",
                title = "Medical-Surgical Nursing",
                author = "Donna Ignatavicius et al.",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_concept",
                isbn = "9780323810341",
                title = "Nursing: A Concept-Based Approach to Learning",
                author = "Pearson Education",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323556295_anat",
                isbn = "9780323556295",
                title = "Anatomy and Physiology",
                author = "Elaine Marieb & Katja Hoehn",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323597120",
                isbn = "9780323597120",
                title = "Pharmacology and the Nursing Process",
                author = "Linda Lane Lilley et al.",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_pub",
                isbn = "9780323810341",
                title = "Community/Public Health Nursing",
                author = "Mary Nies & Melanie McEwen",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_mat",
                isbn = "9780323810341",
                title = "Maternal and Child Health Nursing",
                author = "Adele Pillitteri",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323556295_path",
                isbn = "9780323556295",
                title = "Pathophysiology: The Biologic Basis for Disease in Adults and Children",
                author = "Kathryn McCance & Sue Huether",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_res",
                isbn = "9780323810341",
                title = "Nursing Research: Generating and Assessing Evidence for Nursing Practice",
                author = "Denise Polit & Cheryl Beck",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            ),
            Book(
                id = "nurs_9780323810341_psych",
                isbn = "9780323810341",
                title = "Essentials of Psychiatric Mental Health Nursing",
                author = "Elizabeth Varcarolis",
                department = "School of Nursing",
                availabilityStatus = "AVAILABLE", totalCopies = 5, availableCopies = 5
            )
        )
    }

    /** Parses CSV string content into a list of Book objects.
     * CSV Format: department,title,ISBN,author,total_copies
     */
    fun parseCsvContent(csvContent: String): Pair<List<Book>, Int> {
        val books = mutableListOf<Book>()
        var rejectedCount = 0
        val lines = csvContent.lines()
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (index == 0 && trimmed.lowercase().startsWith("department")) continue

            val parts = trimmed.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 5) {
                val dept = parts[0]
                val title = parts[1]
                val isbn = parts[2]
                val author = parts[3]
                val copies = parts[4].toIntOrNull() ?: 1
                if (title.isNotEmpty() && isbn.isNotEmpty()) {
                    val cleanId = "csv_${System.currentTimeMillis()}_${books.size}"
                    books.add(
                        Book(
                            id = cleanId,
                            title = title,
                            isbn = isbn,
                            author = author,
                            department = dept,
                            availabilityStatus = if (copies > 0) "AVAILABLE" else "UNAVAILABLE",
                            totalCopies = copies,
                            availableCopies = copies
                        )
                    )
                } else {
                    rejectedCount++
                }
            } else {
                rejectedCount++
            }
        }
        return Pair(books, rejectedCount)
    }
}
