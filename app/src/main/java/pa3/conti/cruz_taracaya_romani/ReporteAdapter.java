package pa3.conti.cruz_taracaya_romani;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pa3.conti.cruz_taracaya_.R;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private List<Reporte> reportes;
    private OnReporteClickListener listener;

    public interface OnReporteClickListener {
        void onReporteClick(Reporte reporte);
    }

    public ReporteAdapter(List<Reporte> reportes, OnReporteClickListener listener) {
        this.reportes = reportes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        holder.bind(reportes.get(position));
    }

    @Override
    public int getItemCount() {
        return reportes.size();
    }

    class ReporteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTipo, tvFecha, tvResumen;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvResumen = itemView.findViewById(R.id.tvResumen);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReporteClick(reportes.get(position));
                }
            });
        }

        public void bind(Reporte reporte) {
            String fecha = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(new Date(reporte.timestamp));

            tvTipo.setText(reporte.tipo);
            tvFecha.setText(fecha);

            // Resumen corto (primeros 30 caracteres)
            String resumen = reporte.descripcion.length() > 30 ?
                    reporte.descripcion.substring(0, 30) + "..." :
                    reporte.descripcion;
            tvResumen.setText(resumen);
        }
    }
}