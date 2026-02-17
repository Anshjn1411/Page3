import UIKit
import DotLottie

@objc(DotLottieUIView)
public class DotLottieUIView: UIView {

    @objc public init(url: NSString) {
        super.init(frame: .zero)

        let animation = DotLottieAnimation(
            webURL: url as String,
            config: AnimationConfig(autoplay: true, loop: true)
        )

        let animationView = animation.view()
        animationView.frame = bounds
        animationView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        addSubview(animationView)
    }

    @objc required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
