import SwiftUI
import UIKit
import Shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
            // Custom URL scheme (webshooter://…) cold/warm launches.
            .onOpenURL { url in
                DeepLinkBus.shared.dispatch(rawUrl: url.absoluteString)
            }
            // Universal Links (https://webshooter.se/app/…) routed via the
            // associated-domains entitlement land in NSUserActivity.webpageURL.
            .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                if let url = activity.webpageURL {
                    DeepLinkBus.shared.dispatch(rawUrl: url.absoluteString)
                }
            }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
