# Polyhedroid

NOTE: This project is very much still a work-in-progress, even though progress has somewhat stagnated lately.  
  
Polyhedroid is an Android app for creating to-do lists and managing tasks.  
Tasks are written using Markdown syntax with a few additions (e.g. checkbox lists) and can be rendered back-and-forth between a graphical representation and the underlying markdown.  
  
Works with existing Android CalDav/CardDav accounts on the device, e.g. Google, ProtonMail, or (preferrably) on a personal CalDav server using [DAVx5](https://www.davx5.com) (not affiliated).  
  
This project uses the following libraries:

* [OpenTasks-provider](https://github.com/dmfs/opentasks-provider) (Android tasks provider)
* [Markwon](https://github.com/noties/Markwon) by Noties.io (Markdown rendering)
* [BubbleSeekBar](https://github.com/woxingxiao/BubbleSeekBar)
* [DragLinearLayout](https://github.com/justasm/DragLinearLayout)
* [AdvancedRecyclerView](https://github.com/h6ah4i/android-advancedrecyclerview)

The current version works very well for day-to-day usage - I use it myself to track progress on it and other projects - though the editor view is a little rough around the edges, and there are a few unimplemented features such as the ability to sort notes in the main list view (currently they're sorted by edit date).  
  
Feel free to open an MR to correct any bugs you might find.  
THIS SOFTWARE IS PROVIDED AS-IS WITH NO GUARANTEE OF ACCURACY OR SUPPORT.  
  
I will likely not answer many Issues if you open them.  
I will outright close any issues that do not provide sufficient information such as a debug log or stack trace (e.g. "Doesn't work.")
