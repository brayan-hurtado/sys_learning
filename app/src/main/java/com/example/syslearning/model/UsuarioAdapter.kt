package com.example.syslearning.adapter

import android.view.LayoutInflater
    import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Usuario
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import com.google.android.material.chip.Chip

class UsuarioAdapter(
    private val lista: MutableList<Usuario>,
    private val onClick: (Usuario) -> Unit,


) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val nombre=view.findViewById<TextView>(R.id.txtNombre)
        val correo=view.findViewById<TextView>(R.id.txtCorreo)
        val chipRol = view.findViewById<Chip>(R.id.chipRol)
        val foto = view.findViewById<CircleImageView>(R.id.imgUsuario)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {



        val vista=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario,parent,false)

        return ViewHolder(vista)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val usuario=lista[position]

        holder.nombre.text=usuario.nombre

        holder.correo.text=usuario.correo

        holder.chipRol.text=usuario.rol

        holder.nombre.text = usuario.nombre
        holder.correo.text = usuario.correo
        holder.chipRol.text = usuario.rol

        holder.chipRol.text = usuario.rol

        holder.itemView.setOnClickListener {

            onClick(usuario)

        }

        holder.chipRol.text = usuario.rol

        if (usuario.rol.equals("admin", true)) {

            holder.chipRol.setChipBackgroundColorResource(R.color.azul_admin)

        } else {

            holder.chipRol.setChipBackgroundColorResource(R.color.verde_estudiante)

        }

        if (usuario.foto.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(usuario.foto)
                .into(holder.foto)

        }


    }

    override fun getItemCount(): Int {

        return lista.size

    }

}