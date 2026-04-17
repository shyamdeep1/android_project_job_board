package com.example.android_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.model.AppUser
import com.example.android_project.databinding.ActivityManageUsersBinding

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUsersBinding

    private val users = MockDataProvider.users()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = UsersAdapter(users) { item ->
            Toast.makeText(this, "${item.name} selected", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    inner class UsersAdapter(
        private val items: List<AppUser>,
        private val onClick: (AppUser) -> Unit
    ) : RecyclerView.Adapter<UsersAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvRole: TextView = view.findViewById(R.id.tvRole)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_user, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvRole.text = item.role.name.replace('_', ' ')
            holder.tvSubtitle.text = item.email
            holder.tvStatus.text = if (item.isActive) "Active" else "Pending"
            holder.tvStatus.setTextColor(
                if (!item.isActive) getColor(R.color.statusBadgeGrey)
                else getColor(R.color.figmaPrimaryBtn)
            )
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
