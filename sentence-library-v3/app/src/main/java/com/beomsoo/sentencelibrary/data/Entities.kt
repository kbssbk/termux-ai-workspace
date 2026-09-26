package com.beomsoo.sentencelibrary.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "publications")
data class Publication(@PrimaryKey val id:String,val name:String,val sortOrder:Int=0,val createdAt:Long=System.currentTimeMillis())

@Entity(tableName="issues",foreignKeys=[ForeignKey(entity=Publication::class,parentColumns=["id"],childColumns=["publicationId"],onDelete=ForeignKey.CASCADE)],indices=[Index("publicationId"),Index(value=["officialKey"],unique=true)])
data class Issue(@PrimaryKey val id:String,val publicationId:String,val year:Int,val label:String,val officialKey:String?=null,val sourceUrl:String?=null,val sortOrder:Int=0,val createdAt:Long=System.currentTimeMillis(),val updatedAt:Long=System.currentTimeMillis())

@Entity(tableName="status_definitions")
data class StatusDefinition(@PrimaryKey val id:String,val name:String,val defaultProgress:Int,val adjustable:Boolean,val completed:Boolean,val showOnDashboard:Boolean,val sortOrder:Int)

@Entity(tableName="articles",foreignKeys=[ForeignKey(entity=Issue::class,parentColumns=["id"],childColumns=["issueId"],onDelete=ForeignKey.CASCADE)],indices=[Index("issueId"),Index("statusId"),Index(value=["officialKey"],unique=true)])
data class Article(@PrimaryKey val id:String,val issueId:String,val officialKey:String?=null,val title:String,val sourceUrl:String?=null,val statusId:String="status-unfinished",val progress:Int=0,val summary:String="",val reflection:String="",val application:String="",val sortOrder:Int=0,val createdAt:Long=System.currentTimeMillis(),val updatedAt:Long=System.currentTimeMillis())

@Entity(tableName="quotes",foreignKeys=[ForeignKey(entity=Issue::class,parentColumns=["id"],childColumns=["issueId"],onDelete=ForeignKey.CASCADE),ForeignKey(entity=Article::class,parentColumns=["id"],childColumns=["articleId"],onDelete=ForeignKey.CASCADE)],indices=[Index("issueId"),Index("articleId")])
data class Quote(@PrimaryKey val id:String,val issueId:String,val articleId:String,val sentence:String,val location:String="",val note:String="",val createdAt:Long=System.currentTimeMillis(),val updatedAt:Long=System.currentTimeMillis())

@Entity(tableName="tags")
data class Tag(@PrimaryKey val id:String,val name:String,val sortOrder:Int=0)

@Entity(tableName="quote_tag_cross_ref",primaryKeys=["quoteId","tagId"],foreignKeys=[ForeignKey(entity=Quote::class,parentColumns=["id"],childColumns=["quoteId"],onDelete=ForeignKey.CASCADE),ForeignKey(entity=Tag::class,parentColumns=["id"],childColumns=["tagId"],onDelete=ForeignKey.CASCADE)],indices=[Index("tagId")])
data class QuoteTagCrossRef(val quoteId:String,val tagId:String)

@Entity(tableName="custom_field_definitions")
data class CustomFieldDefinition(@PrimaryKey val id:String,val name:String,val type:String="text",val target:String="article",val sortOrder:Int=0,val enabled:Boolean=true)

@Entity(tableName="custom_field_values",foreignKeys=[ForeignKey(entity=CustomFieldDefinition::class,parentColumns=["id"],childColumns=["fieldId"],onDelete=ForeignKey.CASCADE)],indices=[Index("fieldId"),Index("ownerId")])
data class CustomFieldValue(@PrimaryKey val id:String,val fieldId:String,val ownerId:String,val value:String)

@Entity(tableName="app_preferences") data class AppPreference(@PrimaryKey val key:String,val value:String)
@Entity(tableName="recovery_snapshots") data class RecoverySnapshot(@PrimaryKey val id:String,val reason:String,val json:String,val createdAt:Long=System.currentTimeMillis())
@Entity(tableName="sync_metadata") data class SyncMetadata(@PrimaryKey val officialKey:String,val status:String,val lastAttemptAt:Long,val lastSuccessAt:Long?=null,val error:String?=null)

data class ArticleSummary(val id:String,val year:Int,val issueLabel:String,val title:String,val status:String,val progress:Int,val updatedAt:Long,val publicationName:String="",val issueId:String="")
data class QuoteWithTags(val quote:Quote,val tags:List<Tag>)
