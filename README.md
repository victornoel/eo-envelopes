# Elegant Objects Wrap

Distributed under the GPL3 but don't forget the generated code is not covered by the license.

## Why

See http://www.yegor256.com/2017/01/31/decorating-envelopes.html for an explanation of the requirements.

## How

Enable Annotation Processing in your IDE to take advantage of it during development.

Simply annotate your interfaces with `@GenerateWrap` and a `Wrap` class will be generated for it.
You can now extend it to write a decorating envelope without managing the delegation yourself.

## What

TODO:
- [ ] generics
- [ ] interfaces extending other interfaces
- [ ] interfaces overriding methods of extended interface
- [ ] generate a generic wrapped field
