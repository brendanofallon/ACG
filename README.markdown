ACG is a general-purpose Markov-chain Monte Carlo (MCMC) tool. It advances the chain by performing Metropolis-Hastings update steps, where each step involves proposing some change to a parameter and calculating likelihoods based on the new state. Parameters and likelihoods are defined quite generally and can encompass many different concepts.
	ACG currently has a lot of functionality built around genetic sequences, so it can estimate likelihood distributions of various population genetic parameters, such as population size, recombination rate, and properties of the mutational model. A few nice features are:

* ACG can efficiently compute likelihoods and traverse the space of recombinant genetic sequences. 
* There's a nice GUI that allows monitoring of the state of various chain properties as the chain progresses. 
* A nice (and growing) collection of "Loggers" exists that collect information about the parameters and write it to a file
* ACG is very modular. It's designed to make it very easy to write new Parameters, Likelihoods, Loggers, etc. Once written, they can simply be dropped into the right folder ("plugins/"), and they're ready to use, so sharing should be easy.  

  So far a big focus has been on efficient analysis of recombinant genetic sequences, and ACG can accurately estimate recombination breakpoints from sequences, as well as a host of other features such as time to most recent common ancestor as a function of position along the sequence. 
ACG is still in the early stages of development. Many of the basic features are implemented, but work is ongoing on the GUI and large refactorings are likely in the near future.     
