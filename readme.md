# Installation
- Build project
- Make sure SearchEngines.xml file is located in target folder 
- Create schema in name of 'crawler' in MySQL
- Import sql files in 'CrawlerDatabase' to MySQL (in MySQL Workbench, 'server->data import') 
- Change database information in db.conf file (use your own database information)
- Run 'java -jar target\mavenSpend-1.0-jar-with-dependencies.jar' in cmd line