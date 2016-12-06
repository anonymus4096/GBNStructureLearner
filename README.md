# GBNStructureLearner
Gaussian Bayes Network Structure Learning tool

## Usage
The application has a console input, where many of the parameters can be specified. Here are a few simple examples:

	java –jar GBNStructureLearner.jar –df res/sample.0.data.csv –sf res/sample.0.structure –sa hillclimbing –re 100 –dr 10000
	java –jar GBNStructureLearner.jar –df res/sample.0.data.csv –sf res/sample.0.structure –sa simulatedannealing –ns 100000 –lb 0.2
	java –jar GBNStructureLearner.jar -d res/sample.0.data.csv -st res/sample.0.structure -e -ld myNetwork.txt
	
The complete list of options are the following:
| Option | Long option | Parameter? | Desciption |
| :---: | :---: | :---: | :---: |
| -df| data-filename | Yes | specify file containing data |
| -dr | data-rows | Yes | specify the first how many rows should be used for searching |
| -eo | evaluation-only | No | if you only wish to evaluate the network |

-lb	lambda	Yes	specify how strong the regularization should be (default=0)
-ld	load-from-file	Yes	if you wish to load the network you saved previously, specify the file
-ns	number-of-steps	Yes	specify the number of steps the search algorithm should make (default=10000)
-re	random-edges	Yes	specify how many random edges should the graph contain (default=0)
-sa	search-algorithm	Yes	choose searching algorithm (default=sa)
(options: sa/simulatedannealing/hc/hillclimbing)
-sf	structure-filename	Yes	specify file containing network structure

