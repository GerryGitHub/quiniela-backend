import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var authManager: AuthManager

    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Inicio")
                }

            QuinielasView()
                .tabItem {
                    Image(systemName: "list.bullet")
                    Text("Quinielas")
                }

            GroupsView()
                .tabItem {
                    Image(systemName: "person.3.fill")
                    Text("Grupos")
                }

            ResultadosView()
                .tabItem {
                    Image(systemName: "sportscourt")
                    Text("Resultados")
                }
        }
        .accentColor(Color(hex: "1E88E5"))
    }
}
