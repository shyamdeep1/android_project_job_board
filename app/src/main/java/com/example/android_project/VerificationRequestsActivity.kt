package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.databinding.ActivityVerificationRequestsBinding

data class VerificationItem(
    val companyName: String,
    val info: String,
    var status: String
)

class VerificationRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationRequestsBinding
    private val requests = mutableListOf(
        VerificationItem("Highspeed Studios", "highspeedst@mail.com · Submitted 2h ago", "Pending"),
        VerificationItem("Lunar Djaja Corp.", "lunar@mail.com · Submitted 5h ago", "Pending"),
        VerificationItem("Creative Agency", "creative@mail.com · Submitted 1d ago", "Approved")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvVerifications.layoutManager = LinearLayoutManager(this)
        binding.rvVerifications.adapter = VerificationAdapter(requests)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGoJobs.setOnClickListener {
            startActivity(Intent(this, ManageJobsActivity::class.java))
        }
    }

    inner class VerificationAdapter(private val items: MutableList<VerificationItem>) :
        RecyclerView.Adapter<VerificationAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvCompanyName: TextView = view.findViewById(R.id.tvCompanyName)
            val tvInfo: TextView = view.findViewById(R.id.tvInfo)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val btnReject: TextView = view.findViewById(R.id.btnReject)
            val btnApprove: TextView = view.findViewById(R.id.btnApprove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_verification_request, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvCompanyName.text = item.companyName
            holder.tvInfo.text = item.info
            holder.tvStatus.text = item.status
            holder.tvStatus.setTextColor(
                if (item.status == "Approved") getColor(R.color.figmaPrimaryBtn)
                else getColor(R.color.statusBadgeGrey)
            )

            holder.btnApprove.setOnClickListener {
                item.status = "Approved"
                notifyItemChanged(position)
                Toast.makeText(this@VerificationRequestsActivity, "${item.companyName} approved", Toast.LENGTH_SHORT).show()
            }
            holder.btnReject.setOnClickListener {
                item.status = "Rejected"
                notifyItemChanged(position)
                Toast.makeText(this@VerificationRequestsActivity, "${item.companyName} rejected", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
