The idea is to take a tilt sensor (a wii remote) and map its motions to events. 
The tilt sensor goes on a dancer's hips. So, tilt past a certain boundary defined 
in spherical coordinates (a 'mark'), it calls a method on some object--ultimately 
the idea is to make music. There's a display as well. The Python protoype 
actually doesn't integrate with the wii remote--I switched to Java before I did 
that.

There's a fair amount of structure built around that. If you cross in one 
direction but not the other, you call the method. You invoke a closure 
(implementing the Command pattern via functional programming), or a list of 
closures. Boundaries come in sets, which can be turned on and off independently. 

This code was as much about exploring some new (to me) features of Python as it 
was about implementing the boogiepants prototype. For example, meth_dict in 
bp_display is a dict subclass that allows you to specify two method names on 
creation. When you add an object to meth_dict, it calls one method on that 
object, and when you take it out it calls another. This is used to turn on and 
off the sets mentioned above. Pretty big departure from Java.

rhythm_average was also interesting. It's a class that smooths the rhythm of 
events as they are called. As they arrive in its queue, they are given a 
time-stamp. It only keeps events for a specific interval of time. On a separate 
thread, it puts out events at every (interval / number of events seconds), and 
removes expired events from the queue.

The overall code has a pretty simple MVC arrangement. The entry point is the 
boogiepants object in the controller module. 

The mark module contains the model classes, which define a pretty simple little 
DSL to create different moves a dancer can make. The marks module is where the 
example of that DSL is written, and where the things displayed in the app get 
defined. Having worked with Django and Groovy/Grails since, I'd probably do the 
implementation behind it more simply now.

The bp_display module is the main display module. It also uses sphere_line.

To run it, install vpython, unzip and cd to the directory, go to idle and type

>>> from controller import boogiepants
>>> boogiepants()

And a screen showing a bowl and some buttons should come up. Click the buttons, 
see the sets of marks appear and disappear.

the unit tests are in the bp_test directory. They're py.test test cases. Not 
full coverage, obviously, but I was also learning about that too.
