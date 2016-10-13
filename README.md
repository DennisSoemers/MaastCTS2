# MaastCTS2

Overview
---------

Source code of the MaastCTS2 agent for General Video Game playing. Champion of the 2016 GVG-AI Single-Player Track, and runner-up of the 2016 GVG-AI Two-Player Track.
This repository contains code for both the Single-Player and Two-Player variants.

The majority of the Single-Player variant was implemented for my Master Thesis, written at the Department of Data Science and Knowledge Engineering of Maastricht
University, for the Master of Science in Artificial Intelligence program. After finishing that thesis, I continued modifying the agent a bit more for the competitions
at the [GECCO 2016](http://gecco-2016.sigevo.org/) and [IEEE CIG 2016](http://cig16.image.ece.ntua.gr/) conferences. The source code in this repository is the latest version
(meaning, the version submitted to the CIG competition).

The Two-Player variant at the [WCCI 2016](http://www.wcci2016.org/) competition was simply the Single-Player variant, with a number of features disabled that were not 
expected to work well in a two-player setting (though this has not been verified by experiments yet). For the competition at CIG 2016, it was modified a bit more. The 
source code of this variant is not as clean as I'd like. This is because it started out as a Single-Player variant, than had various parts of code cut out, and some other 
blocks of code inserted. It has been included nevertheless.

Documentation
-------------
- A description of an earlier version of the agent has been [published](https://dke.maastrichtuniversity.nl/m.winands/documents/CIG2016_GVGAI.pdf) in the proceedings 
of the IEEE CIG 2016 conference.<sup>[1](#cigpaper)</sup> Note that the submission deadline for this conference was earlier than the deadlines for the competitions, 
so this paper does not describe everything. It still does include the most interesting and important features, which had already been implemented and tested at the time.
- Currently, the most extensive documentation of techniques used by the agent is my Master Thesis, for which I will include a link here once it is available online. It 
also doesn't includes all details though, since development continued after finshing the thesis.
- I wrote an informal description of how MaastCTS2 works [here](https://dennissoemers.github.io/jekyll/update/2016/09/29/the-general-video-game-agent-maastcts2.html).

License
-------
For my source code provided in this repository, see the [MIT License](./LICENSE). Please note that my agent includes some code from libraries, for which different
licenses are included. The code, licenses, and other files from these libraries are in the /gnu/ and /libs/ directories.

Requirements
-------------
The agent requires the [GVG-AI framework](https://github.com/EssexUniversityMCTS/gvgai/) to play games. It also requires Java to be installed (versions 7 or 8 should
be fine, others have not been tested).

Acknowledgements
----------------
Thanks to Torsten Schuster for developing the [MaastCTS](http://www.gvgai.net/view_profile.php?id=237) agent and describing it in 
[his thesis](https://project.dke.maastrichtuniversity.nl/games/files/msc/Schuster_thesis.pdf). The MCTS implementation of this agent was used as a starting point
for the development of MaastCTS2. Thanks to Dr. Mark Winands and Chiara Sironi, M.Sc. for their supervision of my Master Thesis project. Thanks to the
[organisers](http://www.gvgai.net/about_us.php) of the GVG-AI Competition for organising these competitions.

References
----------

<a name="cigpaper">[1]</a>: Soemers, D.J.N.J., Sironi, C.F., Schuster, T., and Winands, M.H.M. (2016). [Enhancements for Real-Time Monte-Carlo Tree Search in General Video 
Game Playing](https://dke.maastrichtuniversity.nl/m.winands/documents/CIG2016_GVGAI.pdf). In *2016 IEEE Conference on Computational Intelligence and Games (CIG 2016)*.