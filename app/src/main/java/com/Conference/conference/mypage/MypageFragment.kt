package com.Conference.conference.mypage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.Conference.conference.GoogleLoginActivity
import com.Conference.conference.R
import com.Conference.conference.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MypageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    var userNickname = ""
    var myPostCount = 0
    var myCommentCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        user = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userNickname = user.currentUser?.email?.split('@')!![0]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        checkVerify()
        checkPostCount()
        checkCommentCount()
        checkFavoriteCount()

        binding.mypageVerifyIv.setOnClickListener {
            val dialog = VerifyDialog(requireContext())
            dialog.showDialog()
        }
        binding.mypageLogoutBtn.setOnClickListener {
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            user.signOut()
            startActivity(Intent(requireContext(), GoogleLoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun checkFavoriteCount() {
        db.collection("user").document(user.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val favoriteCount = it.data?.get("favoritePost") as HashMap<*,*>?
                if (favoriteCount == null ) {
                    binding.myfavoriteCountTv.text = "0 개"
                } else {
                    binding.myfavoriteCountTv.text = favoriteCount?.size.toString() + " 개"
                }
            }
        }
    }

    private fun checkCommentCount() {
        db.collection("post").get().addOnCompleteListener { document ->
            for (item in document.result) {
                db.collection("post").document(item.id)
                    .collection("comment").get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (doc in it.result) {
                                if (doc["nickname"].toString() == userNickname) {
                                    myCommentCount++
                                }
                            }
                        }
                        binding.mycommentCountTv.text = "$myCommentCount 개"
                    }
            }
        }
    }

    private fun checkPostCount() {
        db.collection("post").get().addOnCompleteListener {
            for (document in it.result) {
                if (document["nickname"].toString() == userNickname) {
                    myPostCount++
                }
            }
            binding.mypostCountTv.text = "$myPostCount 개"
        }
    }

    private fun initData() {
        binding.userEmailTv.text = userNickname + "님 환영합니다 :)"
    }

    fun checkVerify() {
        db.collection("user").document(user.currentUser?.uid!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    binding.userVerifyTv.visibility = View.GONE
                    binding.mypageVerifyIv.setImageResource(R.drawable.ic_verify_on)
                    binding.mypageVerifyIv.isClickable = false
                    binding.mypagePb.visibility = View.GONE
                } else {
                    binding.userVerifyTv.text = "회원 인증을 진행해주세요."
                    binding.userVerifyTv.visibility = View.VISIBLE
                    binding.mypageVerifyIv.setImageResource(R.drawable.ic_verify_off)
                    binding.mypagePb.visibility = View.GONE
                }
            }
    }
}