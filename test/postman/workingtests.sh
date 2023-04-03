#!/bin/bash

function SetupEnvironment() {
	clear

	#Setting up NodeJS
	echo "Checking if NodeJS is installed ..."
	if which node > /dev/null
		node -v
    then
        echo "node is installed, skipping installation..."
    else
        echo "Installing node ..."
        npm install n
        n lts
    fi

    #checking npm version
    	echo "Checking npm version ..."
    	npm --v


    #Setting up Newman
    echo "Checking if Newman is installed ..."
	if newman -v
    then
        echo "Newman is installed, skipping..."
    else
        echo "Installing Newman ..."
        npm install newman
        newman --version
        echo "Successfly installed Newman ... "
    fi
}


function runPostmanTests() {

	#postman files sanity check

	#Running postman collections
	echo "Getting access token from OKTA ..."
		newman run OSMT-64-Auth.postman_collection.json -e OSMT-64.postman_environment.json --ignore-redirects 

	echo "Running OSMT API Tests ..."
		newman run OSMT-64.postman_collection.json -e OSMT-64.postman_environment.json --ignore-redirects 

}


SetupEnvironment
runPostmanTests