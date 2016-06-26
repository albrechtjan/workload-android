Sync Logic
========

The synchronization logic defines what entries will be synched under which conditions and how conflict

The sync logic attempts to follow the ideas outlined in [this talk](https://www.youtube.com/watch?v=xHXn3Kg2IQE). 
A few simplifications are made. For example, it is not possible to perform any administrator actions via the App.
The actual implementation of the synchronization process adheres mostly to [this](https://developer.android.com/training/sync-adapters/index.html)
tutorial, the related classes are to be found in the folder `/android/workload/app/src/main/java/com/gmail/konstantin/schubert/workload/sync/`.


The content provider class of the app `SurveyContentProvider.java`, two tables, `lectures` and `workentries`. Each table contains two
special columns which serve the steer the sync process: `STATUS` and `OPERATION`. The first one defines the sync status for each row: 

 * IDLE
 * PENDING
 * TRANSACTING
 * RETRY

The second one defines the operation, which could be `GET`, `POST`, `INSERT`. However, in the current implementation, only 
the `POST` operation is used. When `upating` a row in the SurveyContentProvider, the [content uri](https://developer.android.com/reference/android/content/ContentUris.html) also tells the content proiver whether it should mark the row as `PENDING` for `POST`. 
However, there is no way to mark a row as `PENDING` for `GET` when querying a table from the SurveyContentProvider. 
This is because all rows are updated with the information from remote on every sync.
Therefore, if one wants to upate the tables, one simply has to trigger a sync. This might turn out to be a bottleneck at some point. But synching is hard, and so far this works.

Triggering a sync typically looks like this in the code:

```
	ContentResolver.requestSync(AccountManager.get(mContext).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, new Bundle());
```
but there are also [more sophisticated ways to do so](https://developer.android.com/training/sync-adapters/running-sync-adapter.html).

When a sync starts, the `onPerformSync` method in the `SyncAdapter.java` class is called.

As can be seen at the beginning of the method, the first step is to update all local tables with the user-related data from the remote tables. Of course, local rows that are not in  `IDLE` sync status will not be overwritten. This steps also takes care of deleting rows in the local lectures table that do not exist on the remote. Adding or removing rows to the lectures table can only happen via the website and not via the web api. Of course for each lecture row there is a column which indicates if the user has selected the lecture for data entry.

In the `workentry table`, rows can not be deleted via the web api. The app also does not check if rows have been deleted on the remote end.

(This makes it easy to accidentally introduce a bug that syncs them again from the local tables to the remote. This is a design flaw. TODO: When a user decides to delete his data on the remote (this option exists), the corresponding workload entry rows should also be delted on the local end. Of course we have to be careful to not delte a workload row that simply has not yet been transmitted to the remote end. Tehre is an issue for this: https://github.com/KonstantinSchubert/workload-android/issues/19)

Next in the `onPerformSync` method, the local tables are parsed for `PENDING` rows, the updates are sent to the web API and the information is updated on the remote.

To conclude: To update the local tables from remote, trigger a sync. To update the remote from the local tables, mark the concerned rows as `POST` `PENDING` *and* trigger a sync.

