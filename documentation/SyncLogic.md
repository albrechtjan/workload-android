Sync Logic
========

The synchronization logic defines what entries will be synched under which conditions and how conflicts are resolved.

The sync logic attempts to follow the ideas outlined in [this talk](https://www.youtube.com/watch?v=xHXn3Kg2IQE). 
A few simplifications are made. The implementation of the synchronization process adheres mostly to [this](https://developer.android.com/training/sync-adapters/index.html)
tutorial, the related classes can be found in the folder `/android/workload/app/src/main/java/com/gmail/konstantin/schubert/workload/sync/`.


The  `SurveyContentProvider.java` class implements an Android content provider with two tables, `lectures` and `workentries`. ([Here](https://developer.android.com/guide/topics/providers/content-providers.html) is the Android documentation on content providers.) Each table contains two
special columns which serve to steer the sync process: `STATUS` and `OPERATION`. The first one defines the sync status for each row: 

 * IDLE
 * PENDING
 * TRANSACTING
 * RETRY

The second one defines the operation, which could be `GET`, `POST`, `INSERT`. However, in the current implementation, only 
the `POST` operation is used. When `update`-ing a row in the SurveyContentProvider, the [content uri](https://developer.android.com/reference/android/content/ContentUris.html) also tells the content provider whether it should mark the row as `PENDING` for `POST`. 
However, when `query`-ing a from the content provider, there is no option to mark a row as `PENDING` for `GET`. 
This is because **all** rows on both tables are updated with the information from the remote end **on every sync**.
Therefore, if one wants to upate the tables, one simply has to trigger a sync. This might turn out to be a bottleneck at some point. But synching is hard, and so far this works.

Triggering a sync typically looks like this in the code:

```
	ContentResolver.requestSync(AccountManager.get(mContext).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, new Bundle());
```
but there are also [more sophisticated ways to do so](https://developer.android.com/training/sync-adapters/running-sync-adapter.html).

When a sync starts, the `onPerformSync` method in the `SyncAdapter.java` class is called.

As can be seen at the beginning of the method, the first step is to update all local tables with the user-related data from the remote end. Local rows that are not in  `IDLE` sync status will not be overwritten in order to protect recent local changes.  (We do not check for time stamps or anything. Data might get lost if updates are made via the web interface and the app simultaneously, or even with some delay when the phone is offline.) This first step also takes care of deleting or adding rows in the local lectures table to match the situation on the remote end. Adding or removing available lectures is an administrative task and can only happen via the admin interface of the web service. When the user selects a lecture for data entry, this is simply reflected by marking the lecture as "active" for the user.

In the `workentry table`, rows can only be added via the Android app, not deleted. The app also does not check if rows have been deleted on the remote end.

(The latter makes it easy to accidentally introduce a bug that syncs them again from the local tables to the remote end. This is a design flaw. TODO: The following issue https://github.com/KonstantinSchubert/workload-android/issues/19)

Next in the `onPerformSync` method, the local tables are parsed for `PENDING` rows, the updates are sent to the web API and the information is updated on the remote end.

To conclude: To update the local tables from remote end, trigger a sync. To update the remote end from the local tables, mark the concerned rows as `POST` `PENDING` *and* trigger a sync.

