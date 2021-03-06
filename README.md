# ITAC

[![Build Status](https://travis-ci.org/gemini-hlsw/itac.svg)](https://travis-ci.org/gemini-hlsw/itac)

Welcome to the ITAC project.  This project is built using maven.  If you're not familiar with maven, the following resources are worth your attention: [http://maven.apache.org/ and http://www.sonatype.com/books/maven-book/index.html].
The fundamental command necessary to execute most of the interesting lifecycle steps is mvn install.  Upon running that, you're quickly going to be outraged that it fails because you don't have a backing database setup.  Getting that database set up is outside of the scope of this README, but there are some details in the documentation folder.  We are basing off a postgres installation.  You need databases: itac_test and itac_dev.  Both databases need a user itac with full access.
In addition, you'll need to have the database initialized to some extent.  This process is yet undocumented, but look for .sql files in this directory tree and explore \i in the psql console.  This process has been worked on somewhat.  The key files still are located down in the phase1-data, but Florian has created an exec task that wraps some of the complexity.  Try mvn exec:exec -Ptest, initDatabase for fun and profit.
