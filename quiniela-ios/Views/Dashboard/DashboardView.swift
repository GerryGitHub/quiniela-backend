import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = DashboardViewModel()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Bienvenido")
                            .font(.title2)
                            .foregroundColor(.gray)
                        
                        Text(authManager.currentUser?.nombre ?? "Usuario")
                            .font(.title)
                            .fontWeight(.bold)
                    }
                    .padding(.horizontal)
                    .padding(.top)
                    
                    if viewModel.partidosEnVivo.isEmpty == false {
                        LiveMatchesCard(partidos: viewModel.partidosEnVivo)
                    }
                    
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("Mis Quinielas")
                                .font(.headline)
                            Spacer()
                            NavigationLink(destination: QuinielasView()) {
                                Text("Ver todas")
                                    .font(.subheadline)
                                    .foregroundColor(Color(hex: "1E88E5"))
                            }
                        }
                        
                        if viewModel.quinielas.isEmpty {
                            EmptyQuinielasCard()
                        } else {
                            ForEach(viewModel.quinielas.prefix(3)) { quiniela in
                                NavigationLink(destination: QuinielaDetalleView(quiniela: quiniela)) {
                                    QuinielaCard(quiniela: quiniela)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 20)
            }
            .navigationTitle("Quiniela")
            .navigationBarItems(
                trailing: Button("Cerrar") {
                    authManager.logout()
                }
            )
            .onAppear {
                viewModel.loadData()
            }
            .overlay {
                if viewModel.isLoading {
                    ProgressView()
                }
            }
        }
    }
}

struct LiveMatchesCard: View {
    let partidos: [PartidoDTO]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Circle()
                    .fill(Color.red)
                    .frame(width: 8, height: 8)
                Text("En Vivo")
                    .font(.headline)
                    .foregroundColor(.red)
            }
            
            ForEach(partidos) { partido in
                HStack {
                    Text(partido.equipoLocal)
                        .font(.subheadline)
                    Spacer()
                    Text("\(partido.golesLocalReal ?? 0) - \(partido.golesVisitanteReal ?? 0)")
                        .font(.headline)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(8)
                    Spacer()
                    Text(partido.equipoVisitante)
                        .font(.subheadline)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4)
        .padding(.horizontal)
    }
}

struct QuinielaCard: View {
    let quiniela: QuinielaResumen
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(quiniela.nombre)
                    .font(.headline)
                Text("Código: \(quiniela.codigoInvitacion ?? "N/A")")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 2)
    }
}

struct EmptyQuinielasCard: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "plus.circle")
                .font(.largeTitle)
                .foregroundColor(Color(hex: "1E88E5"))
            Text("No tienes quinielas")
                .font(.headline)
            Text("Crea o únete a una para comenzar")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.white)
        .cornerRadius(12)
    }
}