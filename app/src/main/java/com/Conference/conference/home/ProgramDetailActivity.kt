package com.Conference.conference.home

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.Conference.conference.databinding.ActivityProgramDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_program_detail.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class ProgramDetailActivity : AppCompatActivity() {
    private var _Binding: ActivityProgramDetailBinding? = null
    private val binding get() = _Binding!!
    var db: FirebaseFirestore? = null
    var user: FirebaseAuth? = null
    lateinit var programKey: String
    lateinit var commentAdapter : CommentAdapter
    var verifyState = false
    init {
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()
        db?.collection("user")?.document(user?.currentUser?.uid!!)?.get()?.addOnSuccessListener {
            if (it.exists()) {
                verifyState = true
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _Binding = ActivityProgramDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val url = intent.getStringExtra("href").toString()
        programKey = url.substring(46, 50)
        commentAdapter = CommentAdapter(this,programKey)

        CoroutineScope(Dispatchers.Default).launch {
            val doc = Jsoup.connect(url).get()
            val imageUrl = "https://do.sejong.ac.kr" + doc.select("div.cover").attr("style")
                .replace("background-image:url(", "")
                .replace(");", "")
            withContext(Dispatchers.Main) {
                viewSetting(doc, imageUrl)
                binding.programDetailPb.visibility = View.INVISIBLE
            }
        }
        // ????????????????????? ????????? ?????????????
        swipeRefresh()
    }
    private fun swipeRefresh() {
        binding.refreshLayout.setOnRefreshListener {
            commentAdapter = CommentAdapter(this,programKey)
            binding.pdCommentRv.adapter = commentAdapter
            Handler().postDelayed(1000) {
                if (commentAdapter.itemCount != 0 ) {
                    binding.postDetailCommentZeroTv2.visibility = View.GONE
                } else {
                    binding.postDetailCommentZeroTv2.visibility = View.VISIBLE
                }
                binding.programCommentTv.text = "?????? " + commentAdapter.itemCount.toString() + "???"
                refreshLayout.isRefreshing = false
            }
        }
    }

    private fun viewSetting(doc: Document, imageUrl: String) {
        Glide.with(applicationContext).load(imageUrl).into(binding.programDetailMainIv)
        binding.programDetailFromTv.text =
            doc.select("div.info").select("div.department").text().replace(" ", " - ")
        binding.programDetailTitleTv.text = doc.select("div.title").select("h4").text()
        binding.pdTargetTv.text = "???????????? : " + doc.select("li.target").select("span").text()
        binding.pdGradeTv.text = "??????/?????? : " + doc.select("div.title").select("ul").select("span")[1].text()
        binding.pdMajorTv.text = "?????? : " + doc.select("div.title").select("ul").select("span")[2].text()
        binding.pdTimeTv.text =
            "??????/???????????? : " + doc.select("div.form").select("p")[2].text() + "\n" +
                    "???????????? : " + doc.select("div.form").select("p")[0].text()
        doc.select("div.description div[data-role=wysiwyg-content]").first().children()
            .forEach {
                binding.pdContentTv.append(it.text() + "\n")
            }
        binding.pdContentTv.append("\n" + "?????? : " + doc.select("li.tbody").first().children()[2].text())

        binding.pdCommentWriteBtn.setOnClickListener {
            if (verifyState) {
                val writeCommentDialog = WriteCommentDialog(this, programKey)
                writeCommentDialog.showDialog()
            } else {
                Toast.makeText(applicationContext,"?????? ?????? ????????? ????????? ????????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.pdCommentRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                binding.programCommentTv.text = "?????? " + commentAdapter.itemCount.toString() + "???"
                if (commentAdapter.itemCount != 0 ) {
                    binding.postDetailCommentZeroTv2.visibility = View.GONE
                } else {
                    binding.postDetailCommentZeroTv2.visibility = View.VISIBLE
                }
            }
        }
        binding.programDetailBackBtn.setOnClickListener {
            finish()
        }
        binding.pdWebBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${intent.getStringExtra("href").toString()}"))
            startActivity(intent)
        }
    }
    override fun onDestroy() {
        _Binding = null
        super.onDestroy()
    }
}