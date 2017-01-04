# verifier
This is a server-client application that permits to execute tests on the computer in a very user-friendly way.
This has a lot things to implement before the first version in relased
## TODO:
 - [ ] Server GUI
 - [ ] Assignment builder
 - [x] Time
 - [ ] ask help
 - [ ] bug report
 - [x] exercise randomization (based on user and exercise)
 - [ ] more exercise types
 - [x] order-free names
 - [x] exercise pre-correction (and make sure to send the answers, lol)
 - [ ] Config system
 - [ ] Tls security integration (with config)

## Simple server tutorial
Even if this has a lot of things that need to be implemented, it's still working, so here's a little tutorial in how to make it work:<br>
First get the server and client executables, then start the server from console (it doesn't have a gui yet) and type the port when he asks it.
Once this is done the server will start and you are able to type commands. No assignment isn't loaded yet so every client trying to get one
will need to wait that one is loaded. to load an assignment you need to type "assignment load file" or, for faster typing "ass load file".
If you don't have an assignment you can get the test one with "ass test file",
The assignments are in YAML format so it's suggested to use the .yml extension.
The name before the extension is the id of the assignment, it needs to be unique for every student's assignment, so the suggested name is iso date + subject (ex: "math-2016-12-31").
subject first so in the folder they get ordered by subject, and the same thing for the iso date.
If you use this format the assignments will be ordered nicely in each of the student's directory.
<br>
Next step: register users in the program:
The program needs a class, an username and a password for every user to make sure they do not sign in as others.
You can specify the user name, class and password writing a file inside the users directory, the file will have the class name before the extension (example: "3h.txt" or "5F".txt)
Inside of it every line is a user, so the last string (divided by spaces) is the password and the rest is the username.
Example: if I have a 3h.txt file with inside "Lorenzo rossi passw" i will have the user "lorenzo rossi" in "3h" with the password "passw"
This method is useful because it works with all names: If I write "Alessandro Maria Gaudesi abc123" the username is "alessandro maria gaudesi" and the password is "abc123".
It's important to note that **only the password is case sensitive**.<br>
Once written the login files, if we execute the command "login reload" the users will be loaded and they'll be able to login.
To test this we can use the command "login list" to lis the classes and "login list class" to list the users in a specific class.
If you don't want to write directly the file you can use the command "login register 'class' 'username'" to register the user into the system
and "login save" to save the new classes with the modified content

## Client
The client usage is really simple and user-friendly. The console can be started for login purposes but it's not necessary and can be closed or hided.
Every step goes through a GUI and so there's not much to explain about it.
Remember that everything except the passord is case-insensitive.

